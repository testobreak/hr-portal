# retrieval/retrieval_pipeline.py

from collections import defaultdict


class RetrievalPipeline:

    def __init__(
        self,
        retriever
    ):

        self.retriever = retriever

    # =====================================================
    # MAIN
    # =====================================================

    def retrieve_relevant_chunks(
        self,
        query,
        top_k=8,
        expand_context=True
    ):

        results = self.retriever.search(
            query=query,
            top_k=top_k
        )

        if not results:
            return []

        results = self.deduplicate(
            results
        )

        if expand_context:

            results = self.expand_neighbors(
                results
            )

        results = self.rerank(
            query,
            results
        )

        return results[:top_k]

    # =====================================================
    # DEDUPLICATION
    # =====================================================

    def deduplicate(
        self,
        results
    ):

        seen = set()

        deduped = []

        for item in results:

            chunk = item["chunk"]

            key = chunk.get(
                "chunk_id"
            )

            if key in seen:
                continue

            seen.add(key)

            deduped.append(item)

        return deduped

    # =====================================================
    # CONTEXT EXPANSION
    # =====================================================

    def expand_neighbors(
        self,
        results
    ):

        expanded = []

        lookup = defaultdict(list)

        for chunk in self.retriever.chunks:

            lookup[
                chunk["file"]
            ].append(chunk)

        for item in results:

            expanded.append(item)

            chunk = item["chunk"]

            current_index = chunk.get(
                "chunk_index",
                0
            )

            siblings = lookup[
                chunk["file"]
            ]

            for sibling in siblings:

                sibling_index = sibling.get(
                    "chunk_index",
                    0
                )

                if abs(
                    sibling_index
                    - current_index
                ) <= 1:

                    expanded.append({

                        "score":
                            item["score"] * 0.92,

                        "chunk":
                            sibling
                    })

        return expanded

    # =====================================================
    # RERANK
    # =====================================================

    def rerank(
        self,
        query,
        results
    ):

        query_lower = query.lower()

        for item in results:

            chunk = item["chunk"]

            score = item["score"]

            # =============================================
            # SYMBOL BOOST
            # =============================================

            if query_lower in chunk.get(
                "name",
                ""
            ).lower():

                score += 0.20

            # =============================================
            # FILE BOOST
            # =============================================

            if query_lower in chunk.get(
                "file",
                ""
            ).lower():

                score += 0.15

            # =============================================
            # DOCSTRING BOOST
            # =============================================

            if query_lower in chunk.get(
                "docstring",
                ""
            ).lower():

                score += 0.10

            # =============================================
            # PARENT BOOST
            # =============================================

            if query_lower in str(
                chunk.get("parent", "")
            ).lower():

                score += 0.08

            item["score"] = score

        results.sort(
            key=lambda x: x["score"],
            reverse=True
        )

        return results