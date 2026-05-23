# context/compression.py

from typing import List, Dict, Any


class ContextCompressor:

    def __init__(
        self,
        max_chunk_chars=4000,
        max_tool_output_chars=2000
    ):

        self.max_chunk_chars = max_chunk_chars

        self.max_tool_output_chars = (
            max_tool_output_chars
        )

    # =====================================================
    # CHUNK COMPRESSION
    # =====================================================

    def compress_chunks(
        self,
        chunks: List[Dict[str, Any]]
    ):

        compressed = []

        seen = set()

        for chunk in chunks:

            chunk_id = chunk.get(
                "chunk_id"
            )

            if chunk_id in seen:
                continue

            seen.add(chunk_id)

            code = (
                chunk.get("code")
                or chunk.get("content")
                or ""
            )

            compressed.append({

                "chunk_id":
                    chunk_id,

                "file":
                    chunk.get("file", ""),

                "symbol":
                    chunk.get("name", ""),

                "type":
                    chunk.get("type", ""),

                "score":
                    round(
                        chunk.get("score", 0),
                        4
                    ),

                "content":
                    self.truncate(
                        code,
                        self.max_chunk_chars
                    )
            })

        return compressed

    # =====================================================
    # TOOL OUTPUT COMPRESSION
    # =====================================================

    def compress_tool_outputs(
        self,
        outputs
    ):

        compressed = []

        for output in outputs:

            compressed.append({

                "tool":
                    output.get("tool"),

                "status":
                    output.get("status"),

                "result":
                    self.truncate(
                        str(
                            output.get(
                                "result",
                                ""
                            )
                        ),
                        self.max_tool_output_chars
                    )
            })

        return compressed

    # =====================================================
    # TRUNCATION
    # =====================================================

    def truncate(
        self,
        text,
        limit
    ):

        if not text:
            return ""

        if len(text) <= limit:
            return text

        return (
            text[:limit]
            + "\n\n...[TRUNCATED]..."
        )