"""
RAG Pipeline Evaluation Module

Provides retrieval quality scoring and response faithfulness evaluation.
Ensures only high-confidence results are returned to users.
"""

import logging
import time
from dataclasses import dataclass
from typing import List, Dict, Optional, Tuple
from enum import Enum

logger = logging.getLogger(__name__)


class QualityGrade(str, Enum):
    PASS = "PASS"
    DEGRADED = "DEGRADED"
    FAIL = "FAIL"


@dataclass
class RetrievalMetrics:
    query: str
    num_retrieved: int
    num_after_filtering: int
    avg_similarity: float
    max_similarity: float
    min_similarity: float
    similarity_scores: List[float]
    relevance_threshold: float
    retrieval_latency_ms: float
    grade: QualityGrade

    def to_dict(self) -> Dict:
        return {
            "query": self.query,
            "num_retrieved": self.num_retrieved,
            "num_after_filtering": self.num_after_filtering,
            "avg_similarity": round(self.avg_similarity, 4),
            "max_similarity": round(self.max_similarity, 4),
            "min_similarity": round(self.min_similarity, 4),
            "relevance_threshold": self.relevance_threshold,
            "retrieval_latency_ms": round(self.retrieval_latency_ms, 2),
            "grade": self.grade.value,
        }


@dataclass
class ResponseMetrics:
    faithfulness_score: float
    completeness_score: float
    source_coverage: int
    total_sources: int
    generation_latency_ms: float
    grade: QualityGrade

    def to_dict(self) -> Dict:
        return {
            "faithfulness_score": round(self.faithfulness_score, 4),
            "completeness_score": round(self.completeness_score, 4),
            "source_coverage": self.source_coverage,
            "total_sources": self.total_sources,
            "generation_latency_ms": round(self.generation_latency_ms, 2),
            "grade": self.grade.value,
        }


@dataclass
class PipelineEvaluation:
    retrieval: RetrievalMetrics
    response: Optional[ResponseMetrics]
    overall_grade: QualityGrade
    total_latency_ms: float

    def to_dict(self) -> Dict:
        result = {
            "retrieval": self.retrieval.to_dict(),
            "overall_grade": self.overall_grade.value,
            "total_latency_ms": round(self.total_latency_ms, 2),
        }
        if self.response:
            result["response"] = self.response.to_dict()
        return result


class RetrievalEvaluator:
    """Evaluates retrieval quality by analyzing cosine similarity scores."""

    def __init__(self, relevance_threshold=0.60, high_quality_threshold=0.75, low_quality_threshold=0.50):
        self.relevance_threshold = relevance_threshold
        self.high_quality_threshold = high_quality_threshold
        self.low_quality_threshold = low_quality_threshold

    def evaluate(self, query, documents, similarity_scores, retrieval_latency_ms):
        """Evaluate retrieval quality and return (metrics, passing_indices)."""
        if not documents or not similarity_scores:
            metrics = RetrievalMetrics(
                query=query, num_retrieved=0, num_after_filtering=0,
                avg_similarity=0.0, max_similarity=0.0, min_similarity=0.0,
                similarity_scores=[], relevance_threshold=self.relevance_threshold,
                retrieval_latency_ms=retrieval_latency_ms, grade=QualityGrade.FAIL,
            )
            return metrics, []

        passing_indices = [i for i, s in enumerate(similarity_scores) if s >= self.relevance_threshold]
        passing_scores = [similarity_scores[i] for i in passing_indices]

        avg_sim = sum(passing_scores) / len(passing_scores) if passing_scores else 0.0
        max_sim = max(passing_scores) if passing_scores else 0.0
        min_sim = min(passing_scores) if passing_scores else 0.0

        if avg_sim >= self.high_quality_threshold:
            grade = QualityGrade.PASS
        elif avg_sim >= self.low_quality_threshold:
            grade = QualityGrade.DEGRADED
        else:
            grade = QualityGrade.FAIL

        metrics = RetrievalMetrics(
            query=query, num_retrieved=len(documents), num_after_filtering=len(passing_indices),
            avg_similarity=avg_sim, max_similarity=max_sim, min_similarity=min_sim,
            similarity_scores=passing_scores, relevance_threshold=self.relevance_threshold,
            retrieval_latency_ms=retrieval_latency_ms, grade=grade,
        )
        logger.info(f"Retrieval eval: query='{query[:50]}' retrieved={len(documents)} "
                     f"filtered={len(passing_indices)} avg_sim={avg_sim:.4f} grade={grade.value}")
        return metrics, passing_indices


class ResponseEvaluator:
    """Evaluates response quality using heuristics + optional LLM-as-judge."""

    def __init__(self, llm_model=None):
        self.llm_model = llm_model

    def evaluate(self, query, context_docs, response, generation_latency_ms):
        faithfulness = self._heuristic_faithfulness(context_docs, response)
        completeness = self._heuristic_completeness(query, response)

        if self.llm_model:
            try:
                scores = self._llm_judge(query, context_docs, response)
                faithfulness = scores.get("faithfulness", faithfulness)
                completeness = scores.get("completeness", completeness)
            except Exception as e:
                logger.warning(f"LLM-as-judge failed, using heuristics: {e}")

        source_coverage = self._estimate_source_coverage(context_docs, response)
        avg_score = (faithfulness + completeness) / 2

        if avg_score >= 0.75:
            grade = QualityGrade.PASS
        elif avg_score >= 0.50:
            grade = QualityGrade.DEGRADED
        else:
            grade = QualityGrade.FAIL

        metrics = ResponseMetrics(
            faithfulness_score=faithfulness, completeness_score=completeness,
            source_coverage=source_coverage, total_sources=len(context_docs),
            generation_latency_ms=generation_latency_ms, grade=grade,
        )
        logger.info(f"Response eval: faithfulness={faithfulness:.2f} completeness={completeness:.2f} grade={grade.value}")
        return metrics

    def _heuristic_faithfulness(self, context_docs, response):
        if not context_docs or not response:
            return 0.0
        context_words = set(w for w in " ".join(context_docs).lower().split() if len(w) > 4 and w.isalpha())
        response_words = set(w for w in response.lower().split() if len(w) > 4 and w.isalpha())
        if not context_words or not response_words:
            return 0.0
        overlap = context_words.intersection(response_words)
        return min(len(overlap) / len(response_words) / 0.6, 1.0)

    def _heuristic_completeness(self, query, response):
        if not query or not response:
            return 0.0
        query_words = set(w.lower() for w in query.split() if len(w) > 3 and w.isalpha())
        if not query_words:
            return 0.5
        matches = sum(1 for w in query_words if w in response.lower())
        return matches / len(query_words)

    def _llm_judge(self, query, context_docs, response):
        context_text = "\n---\n".join(context_docs[:3])
        prompt = f"""Score this AI recommendation (0.0-1.0):
USER QUERY: {query}
CONTEXT: {context_text}
RESPONSE: {response}
Score FAITHFULNESS (grounded in context?) and COMPLETENESS (addresses query?).
Respond ONLY with two numbers: faithfulness, completeness (e.g.: 0.85, 0.90)"""
        result = self.llm_model.generate_content(prompt)
        parts = result.text.strip().split(",")
        return {"faithfulness": max(0, min(1, float(parts[0]))), "completeness": max(0, min(1, float(parts[1])))}

    def _estimate_source_coverage(self, context_docs, response):
        if not response:
            return 0
        response_lower = response.lower()
        covered = 0
        for doc in context_docs:
            doc_words = set(w.lower() for w in doc.split() if len(w) > 5 and w.isalpha())
            if doc_words and sum(1 for w in doc_words if w in response_lower) >= 2:
                covered += 1
        return covered
