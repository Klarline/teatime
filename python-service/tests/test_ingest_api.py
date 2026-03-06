"""Integration tests for the ingest API."""
import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_ingest_single_review(mock_retriever):
    """Test ingesting a single review."""
    response = client.post(
        "/ai/ingest/",
        json={
            "blog_id": 1,
            "shop_id": 10,
            "shop_name": "Test Tea Shop",
            "content": "Great tea and cozy atmosphere",
            "title": "Amazing experience",
            "user_name": "TestUser"
        }
    )
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"
    assert "doc_id" in data
    assert data["doc_id"] == "blog_1"
    mock_retriever.add_document.assert_called_once()


def test_ingest_batch_reviews(mock_retriever):
    """Test ingesting multiple reviews."""
    reviews = [
        {
            "blog_id": 1,
            "shop_id": 10,
            "shop_name": "Shop A",
            "content": "Review 1"
        },
        {
            "blog_id": 2,
            "shop_id": 11,
            "shop_name": "Shop B",
            "content": "Review 2"
        }
    ]
    response = client.post("/ai/ingest/batch", json=reviews)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"
    assert data["total"] == 2


def test_ingest_missing_required_field():
    """Test ingest with missing required field."""
    response = client.post(
        "/ai/ingest/",
        json={
            "shop_id": 10,
            "shop_name": "Test",
            "content": "Review"
            # missing blog_id
        }
    )
    assert response.status_code == 422
