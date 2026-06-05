# runtime/runtime.py

from uuid import uuid4

from planning.planner import (
    Planner
)

from execution.action_compiler import (
    ActionCompiler
)

from runtime.execution_engine import (
    ExecutionEngine
)

from runtime.recovery_loop import (
    RecoveryLoop
)

from runtime.diagnostics import (
    DiagnosticsReporter
)

from memory.state_manager import (
    StateManager
)

from execution.executor import (
    Executor
)

from repository.file_manager import (
    FileManager
)

from context.assembler import (
    ContextAssembler
)

from context.prompt_builder import (
    PromptBuilder
)

from llm.ollama_client import (
    OllamaClient
)

from llm.llm_planner import (
    LLMPlanner
)


class Runtime:

    MAX_ITERATIONS = 20

    def __init__(self):

        self.state_manager = (
            StateManager()
        )

        self.planner = Planner()

        self.compiler = (
            ActionCompiler()
        )

        self.context = (
            ContextAssembler()
        )

        self.prompts = (
            PromptBuilder()
        )

        self.llm = OllamaClient()

        self.llm_planner = LLMPlanner(
            llm=self.llm
        )

        self.diagnostics = (
            DiagnosticsReporter()
        )

        self.file_manager = (
            FileManager("..")
        )

        self.executor = Executor(
            self.file_manager,
            self.state_manager
        )

        self.retrieval_pipeline = (
            self.executor.retrieval_pipeline
        )

        self.engine = (
            ExecutionEngine(
                self.executor,
                self.state_manager,
                diagnostics=self.diagnostics,
            )
        )

        self.recovery = (
            RecoveryLoop(
                self.state_manager
            )
        )

    # ==========================================
    # START
    # ==========================================

    def start(self):

        print(
            "\n  🤖  Local Agent Started"
            "\n  ──────────────────────────────────"
            "\n  Type your goal and press Enter."
            "\n  Type 'exit' to quit.\n"
        )

        while True:

            goal = input("  Goal > ").strip()

            if not goal:
                continue

            if goal.lower() in ["exit", "quit"]:
                print("\n  Goodbye.\n")
                break

            self.run_goal(goal)

    # ==========================================
    # TASK
    # ==========================================

    def run_goal(
        self,
        goal
    ):

        task_id = str(uuid4())

        state = (
            self.state_manager.create_task(
                goal=goal,
                task_id=task_id
            )
        )

        # ──────────────────────────────────────────────────────
        # STEP 0 — Early semantic retrieval
        # Retrieve code chunks matching search queries extracted
        # from the goal before building the LLM plan.
        # ──────────────────────────────────────────────────────

        print("\n  🔍  Performing early semantic retrieval...")
        queries = self._extract_search_queries(goal)
        primary = queries[0] if queries else goal
        others = queries[1:] if len(queries) > 1 else None

        chunks = []
        try:
            retrieved_results = self.retrieval_pipeline.retrieve_hierarchical(
                query=primary,
                top_k=10,
                queries=others,
                goal=goal
            )
            chunks = [item["chunk"] for item in retrieved_results]
        except Exception as e:
            print(f"  ⚠️   Early retrieval failed: {e}\n")

        # Persist retrieved chunks into the task state
        for chunk in chunks:
            self.state_manager.add_chunk(chunk)
        if chunks:
            self.state_manager.increment_retrievals()

        context_package = self._build_context_package(chunks)

        # ──────────────────────────────────────────────────────
        # STEP 1 — LLM-driven planning
        # Ask the local LLM: what type of task is this,
        # what files need to change, what's the diagnosis?
        # ──────────────────────────────────────────────────────

        print("\n  🧠  Asking local LLM to analyse your goal…")

        repo_files = self._get_repo_files(goal)
        llm_plan   = self.llm_planner.plan(
            goal=goal,
            repo_files=repo_files,
            known_symbols=context_package.get("symbols"),
            context_package=context_package,
        )

        if llm_plan:
            # Seed state with LLM-resolved targets so planner + repair
            # engine use deterministic targets from the start
            self._apply_llm_plan(state, llm_plan)

            # Show diagnosis BEFORE execution starts
            self.diagnostics.report(
                goal=goal,
                llm_plan=llm_plan,
                retrieved_chunks=chunks,
            )
        else:
            print("  ⚠️   LLM planning unavailable — using keyword fallback\n")

        # ──────────────────────────────────────────────────────
        # STEP 2 — Keyword planner builds the action sequence
        # (uses LLM targets seeded above if available)
        # ──────────────────────────────────────────────────────

        actions = (
            self.planner.create_plan(
                state
            )
        )

        self.state_manager.set_plan(
            actions
        )

        executions = (
            self.compiler.compile(
                actions
            )
        )

        self.state_manager.push_executions(
            executions
        )

        # ──────────────────────────────────────────────────────
        # STEP 3 — Execution loop
        # ──────────────────────────────────────────────────────

        iteration = 0

        while (
            iteration
            < self.MAX_ITERATIONS
        ):

            iteration += 1

            state.runtime_memory.total_iterations += 1

            self.engine.run()

            pending = (
                self.state_manager
                .get_pending_executions()
            )

            if pending:
                continue

            recovered = (
                self.recovery.run()
            )

            if not recovered:
                break

        state.status = "completed"

        self.state_manager.save_state()

        self.print_task_results(state)

        print("  ✅  Task Complete\n")

    # ==========================================
    # RETRIEVAL CONTEXT HELPERS
    # ==========================================

    def _extract_search_queries(
        self,
        goal: str
    ) -> list:
        import re
        queries = [goal]

        # Extract quoted substrings (e.g., "EmployeeService" or 'logging')
        quotes = re.findall(r"['\"]([^'\"]+)['\"]", goal)
        for q in quotes:
            q_clean = q.strip()
            if q_clean and q_clean not in queries:
                queries.append(q_clean)

        # Split CamelCase/camelCase to space-separated
        camel_split = re.sub(
            r"([a-z])([A-Z])",
            r"\1 \2",
            goal
        )
        if camel_split != goal:
            clean_split = re.sub(
                r"[^a-zA-Z0-9\s]",
                " ",
                camel_split
            )
            clean_split = " ".join(
                clean_split.split()
            )
            if clean_split and clean_split not in queries:
                queries.append(clean_split)

        # Extract keywords
        words = re.findall(
            r"[a-zA-Z0-9_\.]+",
            goal
        )
        stop_words = {
            "to", "the", "a", "for", "and",
            "in", "on", "of", "add", "new",
            "with", "from", "lets", "fix",
            "issue", "bug", "implement",
            "change", "changes", "create",
            "write"
        }
        keywords = [
            w for w in words
            if w.lower() not in stop_words
            and len(w) > 2
        ]

        if keywords:
            kw_query = " ".join(keywords)
            if kw_query not in queries:
                queries.append(kw_query)
            for kw in keywords[:3]:
                if kw not in queries:
                    queries.append(kw)

        return queries[:4]

    def _build_context_package(
        self,
        retrieved_chunks: list
    ) -> dict:
        """
        Builds a context package dictionary containing:
        - retrieved_chunks: the list of raw chunk dicts
        - unique_files: list of unique file paths in retrieval
        - symbols: list of unique symbols (classes/methods) in retrieval
        """
        unique_files = []
        symbols = []
        seen_files = set()
        seen_symbols = set()

        for chunk in retrieved_chunks:
            f = chunk.get("file") or chunk.get("file_path")
            if f and f not in seen_files:
                seen_files.add(f)
                unique_files.append(f)

            symbol_name = chunk.get("name") or chunk.get("symbol")
            if symbol_name:
                sym_key = (f, symbol_name)
                if sym_key not in seen_symbols:
                    seen_symbols.add(sym_key)
                    symbols.append({
                        "name": symbol_name,
                        "type": chunk.get("type", "unknown"),
                        "file": f or "unknown"
                    })

        return {
            "retrieved_chunks": retrieved_chunks,
            "unique_files": unique_files,
            "symbols": symbols
        }

    # ==========================================
    # LLM PLAN APPLICATION
    # ==========================================

    def _apply_llm_plan(self, state, llm_plan: dict) -> None:
        """
        Seed the task state with information from the LLM plan so that
        the planner, repair engine, and target ranker all start with
        concrete, LLM-derived targets rather than empty lists.
        """
        # Map LLM task_type → internal action type
        type_map = {
            "bug_fix":  "bug_fix",
            "feature":  "feature",
            "refactor": "refactor",
            "analysis": "analysis",
        }
        if hasattr(state, "task_type"):
            state.task_type = type_map.get(
                llm_plan.get("task_type", ""), "unknown"
            )

        # Seed target files
        targets = llm_plan.get("target_files", [])
        if targets:
            if not hasattr(state, "modification_targets") or state.modification_targets is None:
                state.modification_targets = []
            for t in targets:
                if t not in state.modification_targets:
                    state.modification_targets.append(t)

        # Store diagnosis as an architecture note (visible to ModificationEngine)
        diagnosis = llm_plan.get("diagnosis", "")
        if diagnosis:
            if not hasattr(state, "architecture_notes") or state.architecture_notes is None:
                state.architecture_notes = []
            state.architecture_notes.append(f"LLM Diagnosis: {diagnosis}")

        # Store suggested queries for retrieval
        queries = llm_plan.get("suggested_queries", [])
        if queries and hasattr(state, "suggested_queries"):
            state.suggested_queries = queries

    # ==========================================
    # REREPO FILE LIST
    # ==========================================

    def _get_repo_files(self, goal: str) -> list:
        """
        Get a list of source files from the repo ranked by relevance to the goal.
        Excludes build output, node_modules, .git etc.
        """
        try:
            import re
            def tokenize(text: str) -> set[str]:
                # Split camelcase and extract alphanumeric tokens
                text = re.sub(r"([a-z])([A-Z])", r"\1 \2", text)
                words = re.findall(r"[a-zA-Z0-9]+", text.lower())
                stop_words = {"to", "the", "a", "for", "and", "in", "on", "of", "add", "new", "with", "from"}
                return {w for w in words if w not in stop_words and len(w) > 2}

            all_files = self.file_manager.list_files()
            if not isinstance(all_files, list):
                return []

            filtered_files = [
                f for f in all_files
                if not any(
                    skip in str(f)
                    for skip in [
                        "node_modules", ".git", "target/",
                        "__pycache__", ".class", ".jar",
                        ".agent", "dist/", "build/",
                    ]
                )
            ]

            # Tokenize goal and score files based on match overlaps
            goal_tokens = tokenize(goal)
            scored_files = []
            for f in filtered_files:
                f_str = str(f).lower()
                score = sum(1 for token in goal_tokens if token in f_str)
                scored_files.append((score, f))

            # Sort by score descending
            scored_files.sort(key=lambda x: x[0], reverse=True)

            # Prioritize files that matched goal keywords.
            # If there are no matches, fall back to top 20 files.
            matched_files = [str(f) for score, f in scored_files if score > 0]
            if matched_files:
                return matched_files[:20]

            return [str(f) for score, f in scored_files[:20]]

        except Exception:
            return []

    # ==========================================
    # RESULTS
    # ==========================================

    def print_task_results(self, state):
        print("\n" + "  " + "═" * 66)
        print("  📊  TASK RESULTS")
        print("  " + "═" * 66)
        print(f"  Goal      {state.goal}")
        print(f"  Status    {state.status.upper()}")
        print(f"  Steps     {state.runtime_memory.total_iterations} iterations")
        print("  " + "─" * 66)

        # Retrieved code
        if state.retrieved_chunks:
            seen = set()
            unique = []
            for chunk in state.retrieved_chunks:
                key = (chunk.get("file"), chunk.get("name"))
                if key not in seen:
                    seen.add(key)
                    unique.append(chunk)

            print(f"\n  📂  Top code matches ({len(unique)} unique symbols):\n")
            for i, chunk in enumerate(unique[:5], 1):
                file_name = chunk.get("file", "?")
                symbol    = chunk.get("name", "")
                sym_type  = chunk.get("type", "")
                label = f"[{i}] {file_name}"
                if symbol:
                    label += f" :: {symbol}"
                if sym_type:
                    label += f"  ({sym_type})"
                print(f"      {label}")

        # Modified files
        modified = getattr(state, "modified_files", []) or []
        if modified:
            print(f"\n  ✏  Modified files ({len(modified)}):\n")
            for f in modified:
                print(f"      {f}")

        # Metrics
        m = state.runtime_memory
        print(f"\n  📈  Metrics:")
        print(f"      tool calls   {m.tool_call_count}")
        print(f"      retrievals   {m.retrieval_count}")
        print(f"      modifications {m.modification_count}")
        print(f"      validations  {m.validation_count}")
        print(f"      repairs ok   {m.successful_repairs}")
        print(f"      repairs fail {m.failed_repairs}")
        print("  " + "═" * 66)