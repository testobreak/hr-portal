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

        self.file_manager = (
            FileManager("..")
        )

        self.executor = Executor(
            self.file_manager,
            self.state_manager
        )

        self.engine = (
            ExecutionEngine(
                self.executor,
                self.state_manager
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
            "Autonomous Runtime Started"
        )

        while True:

            goal = input(
                "\nGoal > "
            )

            if goal.lower() in [
                "exit",
                "quit"
            ]:
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

        print(
            "\nTask Complete"
        )