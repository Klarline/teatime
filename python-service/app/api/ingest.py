from fastapi import APIRouter, HTTPException
from app.models import ReviewDocument
from app.rag.retriever import get_retriever
from typing import List

router = APIRouter(prefix="/ai/ingest", tags=["ingestion"])

@router.post("/")
async def ingest_review(review: ReviewDocument):
    """Ingest a single review into the vector database"""
    try:
        retriever = get_retriever()
        
        # Create document text combining title and content
        doc_text = f"{review.title or ''}\n{review.content}".strip()
        
        # Create metadata
        metadata = {
            "blog_id": review.blog_id,
            "shop_id": review.shop_id,
            "shop_name": review.shop_name,
            "user_name": review.user_name or "Anonymous"
        }
        
        # Add to vector store
        doc_id = f"blog_{review.blog_id}"
        retriever.add_document(doc_id, doc_text, metadata)
        
        return {
            "status": "success",
            "message": f"Review {review.blog_id} ingested successfully",
            "doc_id": doc_id
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/batch")
async def ingest_reviews_batch(reviews: List[ReviewDocument]):
    """Ingest multiple reviews at once"""
    try:
        retriever = get_retriever()
        
        doc_ids = []
        texts = []
        metadatas = []
        
        for review in reviews:
            doc_text = f"{review.title or ''}\n{review.content}".strip()
            metadata = {
                "blog_id": review.blog_id,
                "shop_id": review.shop_id,
                "shop_name": review.shop_name,
                "user_name": review.user_name or "Anonymous"
            }
            
            doc_ids.append(f"blog_{review.blog_id}")
            texts.append(doc_text)
            metadatas.append(metadata)
        
        retriever.add_documents_batch(doc_ids, texts, metadatas)
        
        return {
            "status": "success",
            "message": f"Ingested {len(reviews)} reviews successfully",
            "count": len(reviews)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))