"""Integration tests for the recommend API."""
import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_root_endpoint():
    """Test root endpoint returns service info."""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert "message" in data
    assert "TeaTime" in data["message"]
    assert data.get("status") == "running"


def test_health_endpoint():
    """Test AI health endpoint returns status."""
    response = client.get("/ai/health")
    assert response.status_code == 200
    data = response.json()
    assert "status" in data
    assert "vector_db_count" in data
    assert data["status"] in ("healthy", "unhealthy")


def test_recommend_endpoint_success(mock_pipeline):
    """Test recommend endpoint with mocked pipeline."""
    response = client.post(
        "/ai/recommend",
        json={"query": "quiet cafe for studying", "max_results": 5}
    )
    assert response.status_code == 200
    data = response.json()
    assert "recommendations" in data
    assert "source_blogs" in data
    assert isinstance(data["source_blogs"], list)
    assert data["recommendations"] == (
        "I recommend Downtown Cafe for great tea and Tea House for cozy atmosphere."
    )
    assert data["source_blogs"] == [1, 2]


def test_recommend_endpoint_default_max_results(mock_pipeline):
    """Test recommend uses default max_results when not provided."""
    response = client.post(
        "/ai/recommend",
        json={"query": "best tea"}
    )
    assert response.status_code == 200


def test_recommend_endpoint_invalid_request():
    """Test recommend with invalid/missing query."""
    response = client.post(
        "/ai/recommend",
        json={}
    )
    assert response.status_code == 422  # Validation error
