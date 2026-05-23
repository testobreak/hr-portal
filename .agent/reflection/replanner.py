# reflection/replanner.py

from planning.planner import (
    Planner
)

from reflection.critic import (
    Critic
)

from reflection.repair_engine import (
    RepairEngine
)


class Replanner:

    def __init__(self):

        self.planner = Planner()

        self.critic = Critic()

        self.repair_engine = (
            RepairEngine()
        )

    # =====================================================
    # MAIN
    # =====================================================

    def replan(
        self,
        state
    ):

        critique = self.critic.analyze(
            state
        )

        severity = critique.get(
            "severity",
            "low"
        )

        reflections = critique.get(
            "reflections",
            []
        )

        # =============================================
        # CRITICAL FAILURE
        # =============================================

        if severity == "critical":

            return self.build_critical_plan(
                state,
                critique
            )

        # =============================================
        # LOOP DETECTED
        # =============================================

        if any(

            "loop" in r.lower()

            for r in reflections
        ):

            return self.build_loop_breaking_plan(
                state
            )

        # =============================================
        # HIGH FAILURE
        # =============================================

        if severity == "high":

            return (
                self.repair_engine
                .build_repair_actions(
                    state
                )
            )

        # =============================================
        # DEFAULT REPLAN
        # =============================================

        return self.planner.create_plan(
            state
        )

    # =====================================================
    # CRITICAL RECOVERY
    # =====================================================

    def build_critical_plan(
        self,
        state,
        critique
    ):

        from planning.action_schema import (
            Action
        )

        from planning.action_types import (
            ROLLBACK,
            RETRIEVE,
            ANALYZE
        )

        return [

            Action(

                action_type=ROLLBACK,

                title="Rollback unsafe changes",

                target="rollback",

                reasoning=(
                    "Critical failure recovery"
                ),

                priority=1,

                blocking=True
            ),

            Action(

                action_type=RETRIEVE,

                title="Retrieve stable context",

                target=state.goal,

                reasoning=(
                    "Rebuild repository understanding"
                ),

                priority=2
            ),

            Action(

                action_type=ANALYZE,

                title="Analyze critical failure",

                target="critical recovery",

                reasoning=(
                    "Determine root cause"
                ),

                priority=3
            )
        ]

    # =====================================================
    # LOOP BREAKING
    # =====================================================

    def build_loop_breaking_plan(
        self,
        state
    ):

        from planning.action_schema import (
            Action
        )

        from planning.action_types import (
            RETRIEVE,
            ANALYZE,
            REFLECT
        )

        return [

            Action(

                action_type=REFLECT,

                title="Reflect on repeated failures",

                target="loop analysis",

                reasoning=(
                    "Break repeated execution cycle"
                ),

                priority=1
            ),

            Action(

                action_type=RETRIEVE,

                title="Retrieve alternative context",

                target=state.goal,

                reasoning=(
                    "Gather additional context"
                ),

                priority=2
            ),

            Action(

                action_type=ANALYZE,

                title="Analyze alternative strategy",

                target="alternative approach",

                reasoning=(
                    "Generate new execution path"
                ),

                priority=3
            )
        ]