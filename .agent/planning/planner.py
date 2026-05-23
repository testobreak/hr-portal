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

        task_type = self.classify_task(
            goal
        )

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

        if not targets:

            return [

                Action(
                    action_type=RETRIEVE,
                    title="Retrieve related code",
                    target=state.goal,
                    reasoning="Find related implementation",
                    priority=1
                ),

                Action(
                    action_type=ANALYZE,
                    title="Analyze root cause",
                    target="root cause",
                    reasoning="Determine affected files",
                    priority=2
                )
            ]

        modify_actions = [
            Action(
                action_type=MODIFY,
                title=f"Modify {path}",
                file_path=path,
                target=state.goal,
                reasoning=(
                    "Apply minimal safe fix"
                ),
                priority=3,
                blocking=True
            )
            for path in targets
        ]

        return [

            Action(
                action_type=RETRIEVE,
                title="Retrieve related code",
                target=state.goal,
                reasoning="Find related implementation",
                priority=1
            ),

            Action(
                action_type=ANALYZE,
                title="Analyze root cause",
                target="root cause",
                reasoning="Understand failure source",
                priority=2
            ),

            *modify_actions,

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
        ]

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

        if not targets:

            return [

                Action(
                    action_type=RETRIEVE,
                    title="Retrieve related code",
                    target=state.goal,
                    reasoning="Find related implementation",
                    priority=1
                ),

                Action(
                    action_type=ANALYZE,
                    title="Analyze root cause",
                    target="root cause",
                    reasoning="Determine affected files",
                    priority=2
                )
            ]

        feature_modifications = [
            Action(
                action_type=MODIFY,
                title=f"Implement feature in {path}",
                file_path=path,
                target=state.goal,
                reasoning="Apply feature changes",
                priority=4,
                blocking=True
            )
            for path in targets
        ]

        return [

            Action(
                action_type=RETRIEVE,
                title="Retrieve architecture",
                target=state.goal,
                reasoning=(
                    "Understand existing patterns"
                ),
                priority=1
            ),

            Action(
                action_type=ANALYZE,
                title="Analyze integration points",
                target="integration points",
                reasoning=(
                    "Find affected modules"
                ),
                priority=2
            ),

            Action(
                action_type=MODIFICATION_PLANNING,
                title="Plan modifications",
                target="implementation strategy",
                reasoning=(
                    "Minimize architectural impact"
                ),
                priority=3
            ),

            *feature_modifications,

            Action(
                action_type=VALIDATE,
                title="Validate integration",
                target="validate feature",
                reasoning=(
                    "Check architecture safety"
                ),
                priority=5
            ),

            Action(
                action_type=RUN_BACKEND_TESTS,
                title="Run backend tests",
                target="backend validation",
                reasoning=(
                    "Verify backend behavior"
                ),
                priority=6
            ),

            Action(
                action_type=RUN_FRONTEND_BUILD,
                title="Run frontend build",
                target="frontend validation",
                reasoning=(
                    "Verify frontend compile"
                ),
                priority=7
            ),

            Action(
                action_type=GIT_DIFF,
                title="Review final diff",
                target="git diff",
                reasoning=(
                    "Inspect overall changes"
                ),
                priority=8
            )
        ]

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

        if not targets:

            return [

                Action(
                    action_type=RETRIEVE,
                    title="Retrieve related code",
                    target=state.goal,
                    reasoning="Find related implementation",
                    priority=1
                ),

                Action(
                    action_type=ANALYZE,
                    title="Analyze root cause",
                    target="root cause",
                    reasoning="Determine affected files",
                    priority=2
                )
            ]

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

        return [

            Action(
                action_type=RETRIEVE,
                title="Retrieve impacted code",
                target=state.goal,
                priority=1
            ),

            Action(
                action_type=ANALYZE,
                title="Analyze dependencies",
                target="dependency graph",
                priority=2
            ),

            *refactor_modifications,

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
        ]

    # =====================================================
    # ANALYSIS PLAN
    # =====================================================

    def build_analysis_plan(
        self,
        state: TaskState
    ):

        return [

            Action(
                action_type=RETRIEVE,
                title="Retrieve related code",
                target=state.goal,
                priority=1
            ),

            Action(
                action_type=ANALYZE,
                title="Analyze architecture",
                target=state.goal,
                priority=2
            ),

            Action(
                action_type=SUMMARIZE,
                title="Summarize findings",
                target="analysis summary",
                priority=3
            )
        ]

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

        return [

            Action(
                action_type=RETRIEVE,
                title="Retrieve related code",
                target=state.goal,
                priority=1
            ),

            Action(
                action_type=ANALYZE,
                title="Understand repository context",
                target=state.goal,
                priority=2
            )
        ]

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