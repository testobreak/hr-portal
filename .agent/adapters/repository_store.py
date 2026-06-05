from contextlib import contextmanager
from dataclasses import dataclass
import math
import os

from sqlalchemy import text

from repository_intelligence.db.connection import SessionLocal
from repository_intelligence.indexer.embedding_builder import EmbeddingBuilder
from repository_intelligence.models.file import File
from repository_intelligence.models.repository import Repository
from repository_intelligence.models.symbol import (
    Embedding,
    Symbol,
    SymbolRelationship,
    Test,
)
from repository_intelligence.services.repository_service import RepositoryService


@dataclass
class PersistedEmbedding:
    vector: list[float]
    chunk: dict


class RepositoryStore:
    """
    Adapter boundary between .agent orchestration and repository_intelligence.

    .agent code should depend on this class instead of importing
    repository_intelligence sessions, models, or services directly.
    """

    File = File
    Repository = Repository
    Symbol = Symbol
    SymbolRelationship = SymbolRelationship
    Test = Test

    def __init__(self, repo_root: str | None = None):
        self.repo_root = os.path.abspath(repo_root) if repo_root else None
        self.query_encoder = EmbeddingBuilder()

    @contextmanager
    def session(self):
        session = SessionLocal()
        try:
            yield session
        finally:
            session.close()

    def ensure_modification_chec

























































































































































































































































































                "parent": str(symbol.parent_symbol_id or ""),
                "start_line": symbol.start_line,
                "end_line": symbol.end_line,
                "content": symbol.signature or "",
                "code": symbol.signature or "",
                "level": self._symbol_level(symbol.symbol_type),
                "chunk_index": 0,
                "total_chunks": 1,
            }

        return {
            "chunk_id": f"db:file:{file_row.id if file_row else embedding.id}",
            "source": "database",
            "embedding_id": str(embedding.id),
            "file": file_path,
            "name": file_path,
            "symbol": "",
            "type": "FILE",
            "parent": "",
            "start_line": 1,
            "end_line": 1,
            "content": file_path,
            "code": file_path,
            "level": 1,
            "chunk_index": 0,
            "total_chunks": 1,
        }

    def _symbol_level(self, symbol_type: str) -> int:
        if symbol_type in {"CLASS", "INTERFACE", "ENUM"}:
            return 2

        if symbol_type in {"METHOD", "FUNCTION"}:
            return 3

        return 4

    def _cosine(self, left: list[float], right: list[float]) -> float:
        if not left or not right:
            return 0.0

        limit = min(len(left), len(right))
        dot = sum(left[index] * right[index] for index in range(limit))
        left_norm = math.sqrt(sum(value * value for value in left[:limit]))
        right_norm = math.sqrt(sum(value * value for value in right[:limit]))

        if not left_norm or not right_norm:
            return 0.0

        return dot / (left_norm * right_norm)

