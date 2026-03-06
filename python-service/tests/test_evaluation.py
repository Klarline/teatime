"""Tests for RAG pipeline evaluation module."""

import pytest
from app.rag.evaluation import (
    RetrievalEvaluator, ResponseEvaluator, QualityGrade, ResponseMetrics,
)


class TestRetrievalEvaluator:
    def setup_method(self):
        self.evaluator = RetrievalEvaluator(relevance_threshold=0.60, high_quality_threshold=0.75, low_quality_threshold=0.50)

    def test_high_quality_grades_pass(self):
        metrics, passing = self.evaluator.evaluate("matcha cafe", ["A","B","C"], [0.92,0.85,0.78], 50.0)
        assert metrics.grade == QualityGrade.PASS
        assert len(passing) == 3

    def test_medium_quality_grades_degraded(self):
        metrics, _ = self.evaluator.evaluate("matcha", ["A","B","C"], [0.68,0.62,0.55], 45.0)
        assert metrics.grade == QualityGrade.DEGRADED

    def test_low_quality_grades_fail(self):
        metrics, passing = self.evaluator.evaluate("matcha", ["A","B"], [0.30,0.25], 40.0)
        assert metrics.grade == QualityGrade.FAIL
        assert len(passing) == 0

    def test_filters_low_relevance(self):
        metrics, passing = self.evaluator.evaluate("matcha", ["A","B","C"], [0.88,0.40,0.72], 35.0)
        assert metrics.num_retrieved == 3
        assert metrics.num_after_filtering == 2
        assert 1 not in passing

    def test_empty_documents_grades_fail(self):
        metrics, passing = self.evaluator.evaluate("test", [], [], 10.0)
        assert metrics.grade == QualityGrade.FAIL

    def test_correct_statistics(self):
        metrics, _ = self.evaluator.evaluate("test", ["A","B","C"], [0.90,0.80,0.70], 20.0)
        assert metrics.max_similarity == 0.90
        assert metrics.min_similarity == 0.70
        assert abs(metrics.avg_similarity - 0.80) < 0.01

    def test_to_dict(self):
        metrics, _ = self.evaluator.evaluate("test", ["doc"], [0.85], 15.0)
        d = metrics.to_dict()
        assert d["grade"] in ["PASS", "DEGRADED", "FAIL"]

    def test_custom_thresholds(self):
        strict = RetrievalEvaluator(relevance_threshold=0.80, high_quality_threshold=0.90, low_quality_threshold=0.70)
        metrics, _ = strict.evaluate("test", ["doc"], [0.82], 10.0)
        assert metrics.grade == QualityGrade.DEGRADED


class TestResponseEvaluator:
    def setup_method(self):
        self.evaluator = ResponseEvaluator(llm_model=None)

    def test_faithful_response_scores_high(self):
        ctx = ["Whisk Matcha Cafe has amazing matcha lattes with cozy atmosphere"]
        resp = "I recommend Whisk Matcha Cafe for their amazing matcha lattes. The cozy atmosphere is perfect."
        metrics = self.evaluator.evaluate("matcha cafe", ctx, resp, 500.0)
        assert metrics.faithfulness_score > 0.5

    def test_unfaithful_response_scores_low(self):
        ctx = ["Traditional tea house with premium oolong selection"]
        resp = "Visit the downtown pizza restaurant for great Italian food and pasta."
        metrics = self.evaluator.evaluate("tea house", ctx, resp, 400.0)
        assert metrics.faithfulness_score < 0.5

    def test_completeness_addresses_query(self):
        resp = "For a quiet matcha cafe for studying, try Whisk Matcha with free WiFi."
        metrics = self.evaluator.evaluate("quiet matcha cafe studying",
            ["Whisk Matcha has quiet atmosphere good for studying"], resp, 300.0)
        assert metrics.completeness_score > 0.5

    def test_empty_response_scores_zero(self):
        metrics = self.evaluator.evaluate("test", ["context"], "", 100.0)
        assert metrics.faithfulness_score == 0.0
        assert metrics.grade == QualityGrade.FAIL

    def test_source_coverage(self):
        ctx = ["Whisk Matcha Cafe has excellent matcha lattes and pastries",
               "Nanas Green Tea offers traditional Japanese desserts and beverages"]
        resp = "Whisk Matcha Cafe is known for excellent matcha lattes. Also try Nanas Green Tea for Japanese desserts."
        metrics = self.evaluator.evaluate("matcha", ctx, resp, 200.0)
        assert metrics.source_coverage >= 1

    def test_empty_context(self):
        metrics = self.evaluator.evaluate("test", [], "some response", 100.0)
        assert metrics.faithfulness_score == 0.0

    def test_metrics_to_dict(self):
        m = ResponseMetrics(faithfulness_score=0.85, completeness_score=0.90,
            source_coverage=3, total_sources=5, generation_latency_ms=450.0, grade=QualityGrade.PASS)
        assert m.to_dict()["grade"] == "PASS"
