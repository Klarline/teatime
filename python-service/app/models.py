from pydantic import BaseModel
from typing import Optional, List, Dict

class ReviewDocument(BaseModel):
    blog_id: int
    shop_id: int
    shop_name: str
    content: str
    title: Optional[str] = None
    user_name: Optional[str] = None

class RecommendationRequest(BaseModel):
    query: str
    max_results: int = 5

class RecommendationResponse(BaseModel):
    recommendations: str
    source_blogs: List[int]
    quality: Optional[Dict] = None

class HealthResponse(BaseModel):
    status: str
    vector_db_count: int