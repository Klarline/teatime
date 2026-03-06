"""Unit tests for RAG pipeline."""
import pytest
from unittest.mock import MagicMock, patch


def test_pipeline_recommend_returns_text_and_ids():
    """Test pipeline recommend returns recommendation text and source blog IDs."""
    with patch('app.rag.pipeline.get_retriever') as mock_retriever, \
         patch('app.rag.pipeline.get_generator') as mock_generator:
        retriever = MagicMock()
        retriever.search.return_value = {
            'documents': [['Doc 1', 'Doc 2']],
            'metadatas': [[
                {'blog_id': 1},
                {'blog_id': 2}
            ]],
            'distances': [[0.15, 0.25]]
        }
        mock_retriever.return_value = retriever

        generator = MagicMock()
        generator.generate_recommendation.return_value = "Try these places!"
        mock_generator.return_value = generator

        from app.rag.pipeline import RAGPipeline
        pipeline = RAGPipeline()

        recommendation, source_ids, evaluation = pipeline.recommend("quiet cafe", max_results=5)

        assert recommendation == "Try these places!"
        assert source_ids == [1, 2]
        assert evaluation is not None
        assert evaluation["overall_grade"] in ["PASS", "DEGRADED", "FAIL"]
        retriever.search.assert_called_once_with("quiet cafe", n_results=5)
        generator.generate_recommendation.assert_called_once()


def test_pipeline_recommend_empty_results():
    """Test pipeline when no documents are found."""
    with patch('app.rag.pipeline.get_retriever') as mock_retriever, \
         patch('app.rag.pipeline.get_generator') as mock_generator:
        retriever = MagicMock()
        retriever.search.return_value = {
            'documents': [[]],
            'metadatas': [[]]
        }
        mock_retriever.return_value = retriever

        from app.rag.pipeline import RAGPipeline
        pipeline = RAGPipeline()

        recommendation, source_ids, evaluation = pipeline.recommend("unknown query")

        assert "don't have enough information" in recommendation
        assert source_ids == []
        assert evaluation is None
