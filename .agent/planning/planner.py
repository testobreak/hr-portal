# planning/planner.py

from typing import List

from memory.task_state import (
    TaskState
)

from planning.action_schema import (
    Action
)

from planning.action_types import (
    RETRIEVE,
    ANALYZE,
    MODIFY,
    VALIDATE,
    TEST,
    GIT_DIFF,
    RUN_BACKEND_TESTS,
    RUN_FRONTEND_BUILD,
    MODIFICATION_PLANNING,
    REPAIR,
    REFLECT,
    SUMMARIZE,
    BUG_FIX,
    FEATURE,
    REFACTOR,
    ANALYSIS,
    UNKNOWN
)

class Planner:

    def __init__(self):

        pass

    # =====================================================
    # MAIN ENTRY
    # =====================================================

    def create_plan(
        self,
        state: TaskState
    ) -> List[Action]:

        goal = state.goal.lower()

        # Prefer task_type set by LLMPlanner; fall back to keywords.
        llm_task_type = getattr(state, "task_type", None)
        _type_map = {
            "bug_fix":  BUG_FIX,
            "feature":  FEATURE,
            "refactor": REFACTOR,
            "analysis": ANALYSIS,
        }
        if llm_task_type and llm_task_type in _type_map:
            task_type = _type_map[llm_task_type]
        else:
            task_type = self.classify_task(goal)

        # Check if the plan is already successfully completed
        completed_tools = {
            rec.get("tool")
            for rec in getattr(state, "execution_history", [])
            if rec.get("success")
        }

        completion = self.get_completion_check(state, task_type, list(completed_tools))
        if completion is not None:
            return completion

        # =================================================
        # FAILURE-FIRST
        # =================================================

        if (
            state.test_failures
            or state.failed_steps
        ):

            return self.build_repair_plan(
                state
            )

        # =================================================
        # BUG FIX
        # =================================================

        if task_type == BUG_FIX:

            return self.build_bugfix_plan(
                state
            )

        # =================================================
        # FEATURE
        # =================================================

        elif task_type == FEATURE:

            return self.build_feature_plan(
                state
            )

        # =================================================
        # REFACTOR
        # =================================================

        elif task_type == REFACTOR:

            return self.build_refactor_plan(
                state
            )

        # =================================================
        # ANALYSIS
        # =================================================

        elif task_type == ANALYSIS:

            return self.build_analysis_plan(
                state
            )

        # =================================================
        # FALLBACK
        # =================================================

        return self.build_default_plan(
            state
        )

    # =====================================================
    # TASK CLASSIFICATION
    # =====================================================

    def classify_task(
        self,
        goal: str
    ):

        if any(
            word in goal
            for word in [
                "fix",
                "bug",
                "error",
                "issue",
                "broken",
                "failure",
                "crash"
            ]
        ):

            return BUG_FIX

        elif any(
            word in goal
            for word in [
                "add",
                "implement",
                "create",
                "feature",
                "build"
            ]
        ):

            return FEATURE

        elif any(
            word in goal
            for word in [
                "refactor",
                "cleanup",
                "optimize",
                "improve"
            ]
        ):

            return REFACTOR

        elif any(
            word in goal
            for word in [
                "analyze",
                "explain",
                "understand",
                "review"
            ]
        ):

            return ANALYSIS

        return UNKNOWN

    # =====================================================
    # BUG FIX PLAN
    # =====================================================

    def build_bugfix_plan(
        self,
        state: TaskState
    ):

        targets = self.resolve_target_files(
            state
        )

        has_retrieved = bool(getattr(state, "retrieved_chunks", None))

        if not targets:
            plan = []
            if not has_retrieved:
                plan.append(
                    Action(
                        action_type=RETRIEVE,
                        title="Retrieve related code",
                        target=self._retrieve_query(state),
                        reasoning="Find related implementation",
                        priority=1
                    )
                )
            plan.append(
                Action(
                    action_type=ANALYZE,
                    title="Analyze root cause",
                    target="root cause",
                    reasoning="Determine affected files",
                    priority=2
                )
            )
            return plan

        modify_actions = [
            Action(
                action_type=MODIFY,
                title=f"Modify {path}",
                file_path=path,
                target=self._retrieve_query(state),
                reasoning="Apply minimal safe fix",
                priority=3,
                blocking=True
            )
            for path in targets
        ]

        plan = []
        if not has_retrieved:
            plan.append(
                Action(
                    action_type=RETRIEVE,
                    title="Retrieve related code",
                    target=self._retrieve_query(state),
                    reasoning="Find related implementation",
                    priority=1
                )
            )
            plan.append(
                Action(
                    action_type=ANALYZE,
                    title="Analyze root cause",
                    target="root cause",
                    reasoning="Understand failure source",
                    priority=2
                )
            )

        plan.extend(modify_actions)
        plan.extend([
            Action(
                action_type=VALIDATE,
                title="Validate modifications",
                target="validate code",
                reasoning="Prevent invalid edits",
                priority=4
            ),
            Action(
                action_type=TEST,
                title="Run verification tests",
                target="affected tests",
                reasoning="Ensure fix correctness",
                priority=5
            ),
            Action(
                action_type=GIT_DIFF,
                title="Inspect final diff",
                target="git diff",
                reasoning="Review generated changes",
                priority=6
            )
        ])
        return plan

    # =====================================================
    # FEATURE PLAN
    # =====================================================

    def build_feature_plan(
        self,
        state: TaskState
    ):

        targets = self.resolve_target_files(
            state
        )

        has_retrieved = bool(getattr(state, "retrieved_chunks", None))

        if not targets:
            plan = []
            if not has_retrieved:
                plan.append(
                    Action(
                        action_type=RETRIEVE,
                        title="Retrieve related code",
                        target=self._retrieve_query(state),
                        reasoning="Find related implementation",
                        priority=1
                    )
                )
            plan.append(
                Action(
                    action_type=ANALYZE,
                    title="Analyze root cause",
                    target="root cause",
                    reasoning="Determine affected files",
                    priority=2
                )
            )
            return plan

        feature_modifications = [
            Action(
                action_type=MODIFY,
                title=f"Implement feature in {path}",
                file_path=path,
                target=self._retrieve_query(state),
                reasoning="Apply feature changes",
                priority=4,
                blocking=True
            )
            for path in targets
        ]

        plan = []
        if not has_retrieved:
            plan.append(
                Action(
                    action_type=RETRIEVE,
                    title="Retrieve architecture",
                    target=self._retrieve_query(state),
                    reasoning="Understand existing patterns",
                    priority=1
                )
            )
            plan.append(
                Action(
                    action_type=ANALYZE,
                    title="Analyze integration points",
                    target="integration points",
                    reasoning="Find affected modules",
                    priority=2
                )
            )

        plan.append(
            Action(
                action_type=MODIFICATION_PLANNING,
                title="Plan modifications",
                target="implementation strategy",
                reasoning="Minimize architectural impact",
                priority=3
            )
        )

        plan.extend(feature_modifications)
        plan.extend([
            Action(
                action_type=VALIDATE,
                title="Validate integration",
                target="validate feature",
                reasoning="Check architecture safety",
                priority=5
            ),
            Action(
                action_type=RUN_BACKEND_TESTS,
                title="Run backend tests",
                target="backend validation",
                reasoning="Verify backend behavior",
                priority=6
            ),
            Action(
                action_type=RUN_FRONTEND_BUILD,
                title="Run frontend build",
                target="frontend validation",
                reasoning="Verify frontend compile",
                priority=7
            ),
            Action(
                action_type=GIT_DIFF,
                title="Review final diff",
                target="git diff",
                reasoning="Inspect overall changes",
                priority=8
            )
        ])
        return plan

    # =====================================================
    # REFACTOR PLAN
    # =====================================================

    def build_refactor_plan(
        self,
        state: TaskState
    ):

        targets = self.resolve_target_files(
            state
        )

        has_retrieved = bool(getattr(state, "retrieved_chunks", None))

        if not targets:
            plan = []
            if not has_retrieved:
                plan.append(
                    Action(
                        action_type=RETRIEVE,
                        title="Retrieve related code",
                        target=self._retrieve_query(state),
                        reasoning="Find related implementation",
                        priority=1
                    )
                )
            plan.append(
                Action(
                    action_type=ANALYZE,
                    title="Analyze root cause",
                    target="root cause",
                    reasoning="Determine affected files",
                    priority=2
                )
            )
            return plan

        refactor_modifications = [
            Action(
                action_type=MODIFY,
                title=f"Perform refactor in {path}",
                file_path=path,
                target="safe refactor",
                priority=3,
                blocking=True
            )
            for path in targets
        ]

        plan = []
        if not has_retrieved:
            plan.append(
                Action(
                    action_type=RETRIEVE,
                    title="Retrieve impacted code",
                    target=self._retrieve_query(state),
                    priority=1
                )
            )
            plan.append(
                Action(
                    action_type=ANALYZE,
                    title="Analyze dependencies",
                    target="dependency graph",
                    priority=2
                )
            )

        plan.extend(refactor_modifications)
        plan.extend([
            Action(
                action_type=VALIDATE,
                title="Validate refactor",
                target="refactor validation",
                priority=4
            ),
            Action(
                action_type=TEST,
                title="Run regression tests",
                target="regression testing",
                priority=5
            )
        ])
        return plan

    # =====================================================
    # ANALYSIS PLAN
    # =====================================================

    def build_analysis_plan(
        self,
        state: TaskState
    ):

        has_retrieved = bool(getattr(state, "retrieved_chunks", None))

        plan = []
        if not has_retrieved:
            plan.append(
                Action(
                    action_type=RETRIEVE,
                    title="Retrieve related code",
                    target=self._retrieve_query(state),
                    priority=1
                )
            )

        plan.extend([
            Action(
                action_type=ANALYZE,
                title="Analyze architecture",
                target=self._retrieve_query(state),
                priority=2
            ),
            Action(
                action_type=SUMMARIZE,
                title="Summarize findings",
                target="analysis summary",
                priority=3
            )
        ])
        return plan

    # =====================================================
    # REPAIR PLAN
    # =====================================================

    def build_repair_plan(
        self,
        state: TaskState
    ):

        targets = self.resolve_target_files(
        state
    )

        return [

            Action(
                action_type=ANALYZE,
                title="Analyze failures",
                target="failure analysis",
                reasoning=(
                    "Determine failure root cause"
                ),
                priority=1
            ),

            Action(
                action_type=REPAIR,
                title="Repair failing code",
                target="repair implementation",
                reasoning=(
                    "Attempt autonomous repair"
                ),
                priority=2,

                metadata={
                 "targets": targets
     }
            ),

            Action(
                action_type=VALIDATE,
                title="Validate repairs",
                target="repair validation",
                reasoning=(
                    "Ensure repair safety"
                ),
                priority=3
            ),

            Action(
                action_type=TEST,
                title="Re-run tests",
                target="verification tests",
                reasoning=(
                    "Verify repair success"
                ),
                priority=4
            ),

            Action(
                action_type=REFLECT,
                title="Reflect on failures",
                target="reflection",
                reasoning=(
                    "Capture repair learnings"
                ),
                priority=5
            )
        ]

    # =====================================================
    # DEFAULT PLAN
    # =====================================================

    def build_default_plan(
        self,
        state: TaskState
    ):

        has_retrieved = bool(getattr(state, "retrieved_chunks", None))

        plan = []
        if not has_retrieved:
            plan.append(
                Action(
                    action_type=RETRIEVE,
                    title="Retrieve related code",
                    target=self._retrieve_query(state),
                    priority=1
                )
            )

        plan.append(
            Action(
                action_type=ANALYZE,
                title="Understand repository context",
                target=self._retrieve_query(state),
                priority=2
            )
        )
        return plan

    # =====================================================
    # TARGET RESOLUTION
    # =====================================================

    def resolve_target_files(
        self,
        state: TaskState
    ) -> List[str]:

        #
        # Highest confidence source
        #

        if getattr(
            state,
            "modification_targets",
            None
        ):

            return list(
                dict.fromkeys(
                    state.modification_targets
                )
            )[:5]

        #
        # Fallback to retrieved chunks
        #

        files = []

        for chunk in getattr(
            state,
            "retrieved_chunks",
            []
        ):

            path = chunk.get("file")

            if (
                path
                and path not in files
            ):
                files.append(path)

        if files:

            return files[:5]

        #
        # Last resort:
        # use repository root context
        #

        return []

    def _retrieve_query(self, state: TaskState) -> str:
        """
        Return the best retrieval query for this state.
        Uses LLM suggested_queries if available, otherwise goal.
        """
        queries = getattr(state, "suggested_queries", []) or []
        if queries:
            return " | ".join(queries[:2])
        return state.goal

    def get_completion_check(self, state: TaskState, task_type: str, completed_tools: List[str]) -> List[Action]:
        # If a terminal summarize step for analysis, or test/validation step has successfully completed, we are done!
        if task_type == ANALYSIS and "summarize_state" in completed_tools:
            return []
        elif task_type in (BUG_FIX, REFACTOR) and ("run_backend_tests" in completed_tools):
            return []
        elif task_type == FEATURE and ("run_backend_tests" in completed_tools or "run_frontend_build" in completed_tools):
            return []
        elif task_type == UNKNOWN and ("list_files" in completed_tools or "semantic_retrieve" in completed_tools):
            return []
        return None