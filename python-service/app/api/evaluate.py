"""Evaluation API - quality metrics and detailed evaluation endpoints."""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import Optional, Dict, List
from app.rag.pipeline import get_pipeline
from app.rag.validation import ReviewValidator
import logging

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/ai", tags=["evaluation"])
_validator = ReviewValidator()


class EvaluateRequest(BaseModel):
    query: str = Field(..., min_length=3)
    max_results: int = Field(5, ge=1, le=10)


@router.post("/evaluate")
async def evaluate_recommendation(request: EvaluateRequest):
    """Generate recommendation WITH full quality evaluation metrics."""
    try:
        pipeline = get_pipeline()
        recommendation, blog_ids, evaluation = pipeline.recommend(
            query=request.query, max_results=request.max_results, evaluate=True,
        )
        return {"recommendations": recommendation, "source_blogs": blog_ids, "evaluation": evaluation}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/metrics")
async def get_pipeline_metrics():
    """Return aggregate RAG pipeline quality metrics."""
    try:
        pipeline = get_pipeline()
        return {"pipeline": pipeline.get_pipeline_stats(), "ingestion_validation": _validator.get_stats()}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/validate")
async def validate_review(request: dict):
    """Validate review content quality before ingestion (preview without ingesting)."""
    try:
        report = _validator.validate(
            content=request.get("content", ""), shop_id=request.get("shop_id"),
            shop_name=request.get("shop_name"), blog_id=request.get("blog_id"),
        )
        return report.to_dict()
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
