"""Pytest configuration and fixtures."""
import pytest
from unittest.mock import MagicMock, patch


@pytest.fixture
def mock_retriever():
    """Mock VectorRetriever for unit tests - patch where it's used in ingest API."""
    with patch('app.api.ingest.get_retriever') as mock:
        retriever = MagicMock()
        retriever.search.return_value = {
            'documents': [['Great tea at Downtown Cafe', 'Cozy atmosphere at Tea House']],
            'metadatas': [[
                {'blog_id': 1, 'shop_name': 'Downtown Cafe'},
                {'blog_id': 2, 'shop_name': 'Tea House'}
            ]]
        }
        retriever.get_count.return_value = 42
        retriever.add_document = MagicMock()
        retriever.add_documents_batch = MagicMock()
        mock.return_value = retriever
        yield retriever


@pytest.fixture
def mock_pipeline():
    """Mock RAG pipeline for unit tests - patch where it's used in recommend API."""
    with patch('app.api.recommend.get_pipeline') as mock:
        pipeline = MagicMock()
        pipeline.recommend.return_value = (
            "I recommend Downtown Cafe for great tea and Tea House for cozy atmosphere.",
            [1, 2]
        )
        mock.return_value = pipeline
        yield pipeline
