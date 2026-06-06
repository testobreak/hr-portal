# context/prompt_builder.py

from typing import Dict, Any, Union
from context.prompt_context import PromptContext
from context.compression import ContextCompressor


class PromptBuilder:

    # =====================================================
    # SYSTEM PROMPT
    # =====================================================

    def build_system_prompt(self) -> str:
        return """
You are an autonomous software engineering agent.

Your objectives:

- analyze repositories
- retrieve relevant code
- execute modifications safely
- validate architecture consistency
- repair failures autonomously
- avoid regressions
- minimize unnecessary edits

==================================================
EXECUTION RULES
==================================================

Always:

- prioritize active failures
- use retrieved code context
- preserve architecture
- prefer minimal diffs
- avoid speculative rewrites
- avoid duplicate modifications
- validate before major edits

==================================================
RETRIEVAL RULES
==================================================

Never modify code before:

1. retrieving related context
2. understanding dependencies
3. analyzing surrounding patterns

==================================================
FAILURE HANDLING
==================================================

If tests fail:

1. analyze root cause
2. identify minimal repair
3. avoid unrelated changes
4. preserve working behavior
"""

    # =====================================================
    # HELPERS
    # =====================================================

    def _format_section(self, title: str, content: str) -> str:
        border = "=" * 50
        return f"{border}\n{title}\n{border}\n\n{content}"

    def build_task_section(self, ctx: PromptContext) -> str:
        return self._format_section("TASK GOAL", ctx.goal or "No goal specified.")

    def build_execution_context(self, ctx: PromptContext) -> str:
        lines = [
            f"Current Step: {ctx.current_step or 'None'}",
            f"Completed Steps: {', '.join(ctx.completed_steps) if ctx.completed_steps else 'None'}",
            f"Failed Steps: {', '.join(ctx.failed_steps) if ctx.failed_steps else 'None'}"
        ]
        return self._format_section("EXECUTION CONTEXT", "\n".join(lines))

    def build_repository_context(self, ctx: PromptContext) -> str:
        targets = ctx.modification_targets or []
        content = "Modification Targets:\n"
        if targets:
            content += "\n".join(f"- {t}" for t in targets)
        else:
            content += "None defined."
        return self._format_section("REPOSITORY CONTEXT", content)

    def build_architecture_notes(self, ctx: PromptContext) -> str:
        notes = ctx.architecture_notes or []
        notes_str = "\n".join(f"- {note}" for note in notes) if notes else "No specific architectural constraints defined."
        return self._format_section("ARCHITECTURE NOTES", notes_str)

    def build_dependency_graph(self, ctx: PromptContext) -> str:
        symbols = []
        for chunk in (ctx.retrieved_chunks or []):
            symbol = chunk.get("symbol") or chunk.get("name") or ""
            if symbol and symbol not in symbols:
                symbols.append(symbol.split(".")[-1])

        if len(symbols) >= 2:
            graph_lines = []
            for i in range(min(len(symbols), 4)):
                indent = "  " * i
                arrow = " -> " if i > 0 else ""
                graph_lines.append(f"{indent}{arrow}{symbols[i]}")
            graph_str = "\n".join(graph_lines)
        else:
            graph_str = "No active dependency graph inferred from current context."

        return self._format_section("DEPENDENCY GRAPH", graph_str)

    def build_files_section(self, ctx: PromptContext) -> str:
        files = []
        for chunk in (ctx.retrieved_chunks or []):
            path = chunk.get("file") or chunk.get("file_path")
            if path and path not in files:
                files.append(path)
        content = "\n".join(f"- {f}" for f in files) if files else "No files in context."
        return self._format_section("FILE", content)

    def build_retrieved_code(self, ctx: PromptContext) -> str:
        """Build a section with retrieved code snippets.

        Uses ContextCompressor to truncate large files to a configurable limit
        (default 1500 characters) and includes up to 10 snippets for richer
        context.
        """
        compressor = ContextCompressor(max_chunk_chars=1500)
        snippets = []
        for chunk in (ctx.retrieved_chunks or []):
            file_path = chunk.get("file") or chunk.get("file_path") or "unknown_file"
            content = chunk.get("content") or chunk.get("code") or ""
            if content:
                truncated = compressor.truncate(content, compressor.max_chunk_chars)
                snippets.append(f"File: {file_path}\nCode:\n{truncated}")
        code_str = "\n\n".join(snippets[:10]) if snippets else "No code snippets retrieved."
        return self._format_section("RETRIEVED CODE", code_str)

    def build_constraints(self, ctx: PromptContext) -> str:
        content = """- Do not introduce new external dependencies.
- Preserve existing transactional boundaries.
- Minimize editing footprint; prioritize active repair and verification."""
        return self._format_section("CONSTRAINTS", content)

    def build_output_contract(self, ctx: PromptContext) -> str:
        if ctx.current_step == "repair_file":
            format_str = "Return ONLY the unified diff of the repair patch."
        else:
            format_str = "1. Explanation of changes\n2. Modified files\n3. Unified diff\n4. Validation results"
        return self._format_section("OUTPUT CONTRACT", format_str)

    # =====================================================
    # EXECUTION PROMPT
    # =====================================================

    def build_execution_prompt(
        self,
        context: Union[PromptContext, Dict[str, Any]]
    ) -> str:
        """
        Enterprise-grade prompt compilation utilizing strongly-typed PromptContext.
        Enforces token-efficient constraints, precise edit targeting, and grounding.
        """
        if isinstance(context, dict):
            ctx = PromptContext.from_dict(context)
        else:
            ctx = context

        sections = [
            self.build_task_section(ctx),
            self.build_files_section(ctx),
            self.build_execution_context(ctx),
            self.build_repository_context(ctx),
            self.build_architecture_notes(ctx),
            self.build_dependency_graph(ctx),
            self.build_retrieved_code(ctx),
            self.build_constraints(ctx),
            self.build_output_contract(ctx)
        ]

        return "\n\n".join(sections)