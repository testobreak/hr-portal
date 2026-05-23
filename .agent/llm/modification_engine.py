# llm/modification_engine.py

import json

from llm.ollama_client import (
    OllamaClient
)


class ModificationEngine:

    def __init__(
        self,
        llm=None
    ):

        self.llm = llm or OllamaClient()

    # =====================================================
    # MODIFY FILE
    # =====================================================

    def modify_file(
       self,
       goal,
       file_path,
       original_content,
       retrieved_context=None,
       execution_context=None,
       architecture_notes=None,
    ):

        prompt = self.build_prompt(
            goal=goal,
            file_path=file_path,
            original_content=original_content,
            retrieved_context=retrieved_context or [],
            execution_context=execution_context or "",
            architecture_notes=architecture_notes or []
        )

        response = self.llm.chat([

            {
                "role": "system",
                "content":
                    self.system_prompt()
            },

            {
                "role": "user",
                "content": prompt
            }
        ])

        return self.extract_code(
            response
        )

    # =====================================================
    # SYSTEM
    # =====================================================

    def system_prompt(self):

        return """
You are a repository-aware software engineer.

Rules:

- preserve architecture
- keep minimal diffs
- avoid unrelated changes
- preserve imports unless necessary
- return ONLY final file content

Do not explain.

Return code only.
"""

    # =====================================================
    # PROMPT
    # =====================================================

    def build_prompt(
        self,
        goal,
        file_path,
        original_content,
        retrieved_context,
        execution_context,
        architecture_notes
    ):
        # Format architecture notes cleanly
        arch_notes_str = ""
        if architecture_notes:
            arch_notes_str = "\n".join(f"- {note}" for note in architecture_notes)
        else:
            arch_notes_str = "None"

        # Format retrieved chunks context cleanly instead of dumping JSON
        retrieved_str = ""
        if retrieved_context:
            for chunk in retrieved_context:
                retrieved_str += f"\nFile: {chunk.get('file', '')}\n"
                if chunk.get('symbol'):
                    retrieved_str += f"Symbol: {chunk.get('symbol', '')} ({chunk.get('type', '')})\n"
                retrieved_str += f"Code:\n{chunk.get('content', '')}\n"
                retrieved_str += "-" * 50 + "\n"
        else:
            retrieved_str = "None"

        return f"""
TASK

{goal}

FILE

{file_path}

EXECUTION CONTEXT

{execution_context}

ARCHITECTURE NOTES

{arch_notes_str}

RELATED CONTEXT

{retrieved_str}

ORIGINAL FILE

{original_content}

OUTPUT

Return the fully modified file.
Do not explain.
Return code only.
"""

    # =====================================================
    # CLEAN RESPONSE
    # =====================================================

    def extract_code(
        self,
        response
    ):

        response = response.strip()

        if response.startswith("```"):

            lines = response.splitlines()

            if lines:
                lines = lines[1:]

            if lines and lines[-1].startswith(
                "```"
            ):
                lines = lines[:-1]

            response = "\n".join(lines)

        return response