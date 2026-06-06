# execution/executor.py

import os
import time
import traceback
from datetime import (
    datetime,
    timezone
)
from typing import List

from context.assembler import ContextAssembler
from context.prompt_builder import PromptBuilder
from memory.execution_history import (
    ExecutionRecord
)
from execution.validators import (
    ExecutionValidator
)
from repository.git_manager import (
    GitManager
)
from repository.patch_manager import (
    PatchManager
)
from repository.write_manager import (
    WriteManager
)
from repository.patch_generator import (
    PatchGenerator
)
from execution.test_runner import (
    TestRunner
)
from reflection.critic import (
    Critic
)
from reflection.replanner import (
    Replanner
)
from llm.modification_engine import (
    ModificationEngine
)
from retrieval.retriever import (
    CodeRetriever
)
from retrieval.retrieval_pipeline import (
    RetrievalPipeline
)
from execution.compile_validator import (
    CompileValidator
)


class Executor:

    def __init__(
        self,
        file_manager,
        state_manager
    ):

        self.file_manager = file_manager

        self.state_manager = (
            state_manager
        )

        # Context assembly components
        self.context_assembler = (
            ContextAssembler()
        )

        self.prompt_builder = (
            PromptBuilder()
        )

        # Semantic retrieval
        self.retriever = CodeRetriever(
            repo_root=file_manager.root
        )

        self.retrieval_pipeline = (
            RetrievalPipeline(
                self.retriever
            )
        )

        self.validator = (
            ExecutionValidator()
        )

        self.compile_validator = (
            CompileValidator(file_manager.root)
        )

        self.git = GitManager(
            file_manager.root
        )

        self.patch_manager = (
            PatchManager(
                file_manager.root
            )
        )

        self.writer = WriteManager(
            file_manager.root
        )

        self.patch_generator = (
            PatchGenerator()
        )

        self.modifier = (
            ModificationEngine()
        )

        self.tests = TestRunner(
            file_manager.root
        )

        self.critic = Critic()

        self.replanner = Replanner()

    def execute(
        self,
        execution
    ):
        """
        Accepts:
        - ExecutableAction
        - Any object exposing to_dict()
        - Plain dict
        """

        if hasattr(
            execution,
            "to_dict"
        ):
            execution = execution.to_dict()

        elif not isinstance(
            execution,
            dict
        ):
            raise TypeError(
                f"Unsupported execution type: "
                f"{type(execution)}"
            )

        started = time.time()

        tool = execution.get(
            "tool"
        )

        args = execution.get(
            "args",
            {}
        )

        record = ExecutionRecord(
            action_id=execution.get(
                "action_id",
                "unknown"
            ),
            tool=tool,
            status="running",
            input_data=execution
        )

        try:

            self.validator.validate_retry(
                execution
            )

            self.validator.validate_execution(
                tool,
                args
            )

            result = self.dispatch(
                tool,
                args
            )

            record.success = True
            record.status = "completed"
            record.output_data = result

            self.state_manager.add_tool_output(
                tool,
                result
            )

            return {
                "success": True,
                "tool": tool,
                "result": result,
                "duration": round(
                    time.time() - started,
                    3
                )
            }

        except Exception as e:

            traceback.print_exc()

            record.success = False
            record.status = "failed"
            record.error = str(e)

            return {
                "success": False,
                "tool": tool,
                "error": str(e)
            }

        finally:

            record.duration_seconds = (
                time.time() - started
            )

            record.completed_at = (
                datetime.now(
                    timezone.utc
                ).isoformat()
            )

            self.state_manager.add_execution_record(
                record
            )

            self.state_manager.increment_tool_calls()

    def _persist_retrieved_chunks(
        self,
        chunks: List[dict]
    ) -> None:
        """
        Persist unified retrieval chunks into agent state so that
        downstream systems can consume them structurally.
        """
        for chunk in chunks:

            self.state_manager.add_chunk(
                chunk
            )

        if chunks:
            self.state_manager.increment_retrievals()

    def dispatch(
        self,
        tool,
        args
    ):

        #
        # filesystem
        #

        if tool == "list_files":

            return self.file_manager.list_files()

        elif tool == "search_code":

            return self.file_manager.search_code(
                args["keyword"]
            )

        elif tool == "semantic_retrieve":

            query = args["query"]

            results = (
                self.retrieval_pipeline
                .retrieve_relevant_chunks(
                    query=query,
                    top_k=8,
                    expand_context=True
                )
            )

            chunks = [
                item["chunk"]
                for item in results
            ]

            self._persist_retrieved_chunks(
                chunks
            )

            return results

        elif tool == "read_file":

            return self.file_manager.read_file(
                path=args["path"],
                start=args.get("start"),
                end=args.get("end")
            )

        #
        # git
        #

        elif tool == "git_status":

            return self.git.status()

        elif tool == "git_diff":

            diff = self.git.diff()

            self.validator.validate_diff_size(
                diff
            )

            return diff

        elif tool == "git_commit":

            message = args.get(
                "message",
                "agent update"
            )

            return self.git.commit(
                message
            )

        elif tool == "rollback_file":

            target = args.get(
                "path"
            )

            if not target:
                raise RuntimeError(
                    "path required"
                )

            return self.git.rollback_file(
                target
            )

        elif tool == "rollback_to_commit":

            commit = args.get(
                "commit"
            )

            if not commit:
                commit = self.git.get_last_checkpoint()

            if not commit:
                raise RuntimeError(
                    "No checkpoint commit hash found for rollback"
                )

            return self.git.rollback_to_commit(
                commit
            )

        elif tool == "create_checkpoint":

            return self.git.create_checkpoint(
                args.get(
                    "message",
                    "checkpoint"
                )
            )

        #
        # modifications
        #

        elif tool == "apply_patch":

            patch = args.get(
                "patch"
            )

            if not patch:

                patch = (
                    self.state_manager
                    .get_last_patch()
                )

            if not patch:

                raise RuntimeError(
                    "No patch available"
                )

            return self.patch_manager.apply_patch(
                patch
            )

        elif tool == "write_file":

            result = self.writer.write_file(
                path=args["path"],
                content=args["content"]
            )

            # Call syntax validation immediately after writing using resolved absolute path
            if result.get("success", False):
                try:
                    self.validator.validate_file_syntax(self.file_manager.resolve_path(args["path"]))
                except Exception as val_err:
                    print(f"[Validation Failure] {args['path']}: {val_err}")
                    # Revert the corrupted file immediately
                    self.git.rollback_file(args["path"])
                    raise RuntimeError(f"Syntax validation failed: {val_err}")

            return result

        elif tool == "modify_file":

            path = args["path"]

            goal = args["goal"]

            try:

                # Stage 1: Generate modified content
                modification_result = self.dispatch(
                    "generate_modification",
                    {
                        "path": path,
                        "goal": goal
                    }
                )

                # Stage 2: Build patch
                patch_result = self.dispatch(
                    "generate_patch",
                    {
                        "path":
                            modification_result["path"],

                        "original":
                            modification_result["original"],

                        "modified":
                            modification_result["modified"]
                    }
                )

                # Stage 3: Apply patch
                apply_result = self.dispatch(
                    "apply_patch",
                    {
                        "patch":
                            patch_result["patch"]
                    }
                )

                success = apply_result.get(
                    "success",
                    False
                )

                validation_error = None
                if success:
                    # Validate the applied modification syntax using resolved absolute path before keeping it
                    try:
                        self.validator.validate_file_syntax(self.file_manager.resolve_path(path))
                    except Exception as val_err:
                        print(f"[Validation Failure] {path}: {val_err}")
                        success = False
                        validation_error = f"Syntax validation failed: {val_err}"

                if success:
                    # Validate the compilation using resolved absolute path
                    try:
                        self.compile_validator.validate(self.file_manager.resolve_path(path))
                    except Exception as comp_err:
                        print(f"[Compilation Failure] {path}: {comp_err}")
                        success = False
                        validation_error = f"Compilation validation failed: {comp_err}"

                if success:
                    # Validate using test suite
                    try:
                        test_res = self.tests.run_backend_tests()
                        if not test_res.get("success", False):
                            success = False
                            validation_error = f"Test suite validation failed: {test_res.get('failures', 'Unknown test failures')}"
                    except Exception as test_err:
                        print(f"[Test Suite Failure] {path}: {test_err}")
                        success = False
                        validation_error = f"Test suite validation failed: {test_err}"

                if not success:

                    self.dispatch(
                        "rollback_file",
                        {
                            "path": path
                        }
                    )

                # Update state safely on lifecycle success
                state = self.state_manager.get_state()

                if success:
                    if hasattr(state, "modified_files"):

                        if state.modified_files is None:
                            state.modified_files = []

                        if path not in state.modified_files:
                            state.modified_files.append(path)

                    if hasattr(state, "generated_code"):

                        if state.generated_code is None:
                            state.generated_code = []

                        state.generated_code.append({
                            "path": path,
                            "content":
                                modification_result["modified"]
                        })

                result = {
                    "success": success,
                    "path": path,
                    "patch": patch_result["patch"]
                }
                if validation_error:
                    result["error"] = validation_error
                return result

            except Exception:

                try:

                    self.dispatch(
                        "rollback_file",
                        {
                            "path": path
                        }
                    )

                except Exception:
                    pass

                raise

        elif tool == "generate_modification":

            original = (
                self.file_manager.read_file(
                    path=args["path"]
                )
            )

            if original is None:

                raise RuntimeError(
                    f"Unable to read file: "
                    f"{args['path']}"
                )

            state = self.state_manager.get_state()

            context = (
                self.context_assembler.build(
                    state
                )
            )

            execution_context = (
                self.prompt_builder
                .build_execution_prompt(
                    context
                )
            )

            modified = (
                self.modifier.modify_file(
                    goal=args["goal"],
                    file_path=args["path"],
                    original_content=original,
                    execution_context=execution_context,
                    architecture_notes=getattr(
                        state,
                        "architecture_notes",
                        []
                    )
                )
            )

            if not modified:

                raise RuntimeError(
                    "Modification engine returned empty content"
                )

            result = {
                "success": True,
                "path": args["path"],
                "original": original,
                "modified": modified
            }

            self.state_manager.set_last_modification(
                result
            )

            return result

        elif tool == "generate_patch":

            payload = args

            if not payload:

                payload = (
                    self.state_manager
                    .get_last_modification()
                )

            if not payload:

                raise RuntimeError(
                    "No modification available"
                )

            patch = (
                self.patch_generator.generate(

                    original_content=
                        payload["original"],

                    modified_content=
                        payload["modified"],

                    path=
                        payload["path"]
                )
            )

            self.validator.validate_patch(
                patch
            )

            self.state_manager.set_last_patch(
                patch
            )

            return {
                "success": True,
                "patch": patch
            }

        #
        # tests
        #

        elif tool == "run_backend_tests":

            return self.tests.run_backend_tests()

        elif tool == "run_frontend_tests":

            return self.tests.run_frontend_tests()

        elif tool == "run_frontend_build":

            return self.tests.run_frontend_build()

        #
        # planner helpers
        #

        elif tool == "summarize_state":

            state = (
                self.state_manager
                .get_state()
            )

            return {
                "goal":
                    getattr(
                        state,
                        "goal",
                        None
                    ),

                "completed_steps":
                    len(
                        getattr(
                            state,
                            "completed_steps",
                            []
                        )
                    ),

                "failures":
                    len(
                        getattr(
                            state,
                            "failed_steps",
                            []
                        )
                    ),

                "retrieved_chunks":
                    len(
                        getattr(
                            state,
                            "retrieved_chunks",
                            []
                        )
                    ),

                "tool_outputs":
                    len(
                        getattr(
                            state,
                            "tool_outputs",
                            []
                        )
                    )
            }

        elif tool == "plan_modifications":

            state = (
                self.state_manager
                .get_state()
            )

            return {
                "goal":
                    getattr(
                        state,
                        "goal",
                        None
                    ),

                "touched_files":
                    getattr(
                        state,
                        "touched_files",
                        []
                    ),

                "active_symbols":
                    getattr(
                        state,
                        "active_symbols",
                        []
                    ),

                "recommendation":
                    (
                        "Analyze retrieved "
                        "architecture before "
                        "performing modifications."
                    )
            }

        #
        # reflection
        #

        elif tool == "reflect":

            state = (
                self.state_manager
                .get_state()
            )

            result = (
                self.critic.analyze(
                    state
                )
            )

            if hasattr(
                self.state_manager,
                "add_reflection"
            ):
                self.state_manager.add_reflection(
                    str(result)
                )

            return result

        elif tool == "replan":

            state = (
                self.state_manager
                .get_state()
            )

            actions = (
                self.replanner.replan(
                    state
                )
            ) or []

            if (
                actions
                and hasattr(
                    self.state_manager,
                    "push_executions"
                )
            ):
                self.state_manager.push_executions(
                    actions
                )

            return {
                "generated_actions":
                    len(actions),

                "actions":
                    actions
            }

        raise RuntimeError(
            f"Unknown tool: {tool}"
        )