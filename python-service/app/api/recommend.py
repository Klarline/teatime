from fastapi import APIRouter, HTTPException
from app.models import RecommendationRequest, RecommendationResponse, HealthResponse
from app.rag.pipeline import get_pipeline
from app.rag.retriever import get_retriever

router = APIRouter(prefix="/ai", tags=["recommendations"])

@router.post("/recommend", response_model=RecommendationResponse)
async def get_recommendations(request: RecommendationRequest):
    """Get AI-powered tea shop recommendations"""
    try:
        pipeline = get_pipeline()
        recommendation_text, source_blog_ids = pipeline.recommend(
            request.query,
            max_results=request.max_results
        )
        
        return RecommendationResponse(
            recommendations=recommendation_text,
            source_blogs=source_blog_ids
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/health", response_model=HealthResponse)
async def ai_health_check():
    """Check AI service health and vector DB status"""
    try:
        retriever = get_retriever()
        doc_count = retriever.get_count()
        
        return HealthResponse(
            status="healthy",
            vector_db_count=doc_count
        )
    except Exception as e:
        return HealthResponse(
            status="unhealthy",
            vector_db_count=0
        )