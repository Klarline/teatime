"""RAG Pipeline with Integrated Quality Evaluation"""

import logging
import time
from typing import Tuple, List, Dict, Optional

from app.rag.retriever import get_retriever
from app.rag.generator import get_generator
from app.rag.evaluation import (
    RetrievalEvaluator, ResponseEvaluator, PipelineEvaluation, QualityGrade,
)

logger = logging.getLogger(__name__)


class RAGPipeline:
    def __init__(self):
        self.retriever = get_retriever()
        self.generator = get_generator()
        self.retrieval_evaluator = RetrievalEvaluator()
        self.response_evaluator = ResponseEvaluator()
        self._execution_history: List[Dict] = []
        self._max_history = 100

    def recommend(self, query: str, max_results: int = 5, evaluate: bool = True) -> Tuple[str, List[int], Optional[Dict]]:
        """
        End-to-end recommendation pipeline with quality evaluation.
        Returns: (recommendation_text, source_blog_ids, evaluation_metrics)
        """
        pipeline_start = time.time()
        evaluation_data = None

        # Step 1: Retrieve
        retrieval_start = time.time()
        search_results = self.retriever.search(query, n_results=max_results)
        retrieval_latency = (time.time() - retrieval_start) * 1000

        if not search_results['documents'][0]:
            return "I don't have enough information about tea shops yet. Please check back after some reviews are added!", [], None

        documents = search_results['documents'][0]
        metadatas = search_results['metadatas'][0]

        # Convert Chroma distances to similarity (1 - distance)
        distances = search_results.get('distances', [[]])[0]
        similarity_scores = [max(0.0, 1.0 - d) for d in distances] if distances else [0.5] * len(documents)

        # Step 2: Evaluate retrieval and filter low-relevance docs
        if evaluate:
            retrieval_metrics, passing_indices = self.retrieval_evaluator.evaluate(
                query=query, documents=documents,
                similarity_scores=similarity_scores, retrieval_latency_ms=retrieval_latency,
            )
            if passing_indices:
                documents = [documents[i] for i in passing_indices]
                metadatas = [metadatas[i] for i in passing_indices]
            else:
                logger.warning(f"All docs filtered for '{query}'. Using unfiltered.")

        # Step 3: Generate recommendation
        gen_start = time.time()
        recommendation = self.generator.generate_recommendation(query, documents, metadatas)
        gen_latency = (time.time() - gen_start) * 1000

        # Step 4: Extract source blog IDs
        source_blog_ids = [int(meta['blog_id']) for meta in metadatas if meta.get('blog_id') is not None]

        # Step 5: Evaluate response quality
        if evaluate:
            response_metrics = self.response_evaluator.evaluate(
                query=query, context_docs=documents,
                response=recommendation, generation_latency_ms=gen_latency,
            )
            total_latency = (time.time() - pipeline_start) * 1000
            overall_grade = self._compute_overall_grade(retrieval_metrics.grade, response_metrics.grade)
            pipeline_eval = PipelineEvaluation(
                retrieval=retrieval_metrics, response=response_metrics,
                overall_grade=overall_grade, total_latency_ms=total_latency,
            )
            evaluation_data = pipeline_eval.to_dict()
            self._track_execution(evaluation_data)
            logger.info(f"Pipeline: query='{query[:50]}' grade={overall_grade.value} latency={total_latency:.0f}ms")

        return recommendation, source_blog_ids, evaluation_data

    def _compute_overall_grade(self, retrieval_grade, response_grade):
        order = {QualityGrade.PASS: 2, QualityGrade.DEGRADED: 1, QualityGrade.FAIL: 0}
        min_val = min(order[retrieval_grade], order[response_grade])
        return next(g for g, v in order.items() if v == min_val)

    def _track_execution(self, data):
        self._execution_history.append(data)
        if len(self._execution_history) > self._max_history:
            self._execution_history = self._execution_history[-self._max_history:]

    def get_pipeline_stats(self):
        if not self._execution_history:
            return {"total_executions": 0}
        total = len(self._execution_history)
        grades = [e["overall_grade"] for e in self._execution_history]
        avg_latency = sum(e["total_latency_ms"] for e in self._execution_history) / total
        sims = [e["retrieval"]["avg_similarity"] for e in self._execution_history if e["retrieval"]["avg_similarity"] > 0]
        faiths = [e["response"]["faithfulness_score"] for e in self._execution_history if e.get("response")]
        return {
            "total_executions": total,
            "pass_rate": round(grades.count("PASS") / total, 4),
            "degraded_rate": round(grades.count("DEGRADED") / total, 4),
            "fail_rate": round(grades.count("FAIL") / total, 4),
            "avg_latency_ms": round(avg_latency, 2),
            "avg_retrieval_similarity": round(sum(sims)/len(sims), 4) if sims else 0.0,
            "avg_faithfulness": round(sum(faiths)/len(faiths), 4) if faiths else 0.0,
        }

_pipeline = None
def get_pipeline() -> RAGPipeline:
    global _pipeline
    if _pipeline is None:
        _pipeline = RAGPipeline()
    return _pipeline
