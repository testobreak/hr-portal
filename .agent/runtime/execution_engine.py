# runtime/execution_engine.py


class ExecutionEngine:

    def __init__(
        self,
        executor,
        state_manager
    ):

        self.executor = executor

        self.state_manager = (
            state_manager
        )

    # =====================================================
    # MAIN
    # =====================================================

    def run(self):
        """
        Execute the next pending action.

        Runtime repeatedly calls this
        until the queue is exhausted.
        """

        pending = (
            self.state_manager
            .get_pending_executions()
        )

        if not pending:
            return None

        execution = pending[0]

        #
        # mark running
        #
        if isinstance(
            execution,
            dict
        ):
            execution["status"] = (
                "running"
            )

        result = (
            self.executor.execute(
                execution
            )
        )

        #
        # remove executed item
        #
        completed = (
            self.state_manager
            .pop_next_execution()
        )

        #
        # update state tracking
        #
        state = (
            self.state_manager
            .get_state()
        )

        action_id = None

        if isinstance(
            completed,
            dict
        ):
            action_id = completed.get(
                "action_id"
            )

        if (
            isinstance(
                result,
                dict
            )
            and result.get(
                "success",
                False
            )
        ):

            if action_id:

                state.completed_steps.append(
                    action_id
                )

        else:

            if action_id:

                state.failed_steps.append(
                    action_id
                )

            #
            # retry if possible
            #
            if (
                completed
                and hasattr(
                    self.state_manager,
                    "retry_execution"
                )
            ):

                retries = completed.get(
                    "retries",
                    0
                )

                max_retries = completed.get(
                    "max_retries",
                    3
                )

                if retries < max_retries:

                    self.state_manager.retry_execution(
                        completed
                    )

        self.state_manager.save_state()

        return result