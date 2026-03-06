from fastapi import APIRouter, HTTPException
from app.models import ReviewDocument
from app.rag.retriever import get_retriever
from app.rag.validation import ReviewValidator
from typing import List
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/ai/ingest", tags=["ingestion"])

# Shared validator (tracks stats across requests)
_validator = ReviewValidator(min_content_length=20, min_quality_score=0.3)

@router.post("/")
async def ingest_review(review: ReviewDocument):
    """Ingest a single review with quality validation"""
    try:
        # Step 1: Validate content quality
        report = _validator.validate(
            content=review.content, shop_id=review.shop_id,
            shop_name=review.shop_name, blog_id=review.blog_id,
        )

        if report.result.value == "REJECTED":
            logger.warning(f"Review rejected: blog_id={review.blog_id} issues={report.issues}")
            return {"status": "rejected", "message": f"Review rejected: {'; '.join(report.issues)}",
                    "validation": report.to_dict()}

        # Step 2: Proceed with ingestion
        retriever = get_retriever()
        doc_text = f"{review.title or ''}\n{review.content}".strip()
        metadata = {
            "blog_id": review.blog_id, "shop_id": review.shop_id,
            "shop_name": review.shop_name, "user_name": review.user_name or "Anonymous"
        }
        doc_id = f"blog_{review.blog_id}"
        retriever.add_document(doc_id, doc_text, metadata)

        status_msg = f"Review {review.blog_id} ingested successfully"
        if report.result.value == "FLAGGED":
            status_msg += f" (flagged: {'; '.join(report.issues)})"

        logger.info(f"Ingested: blog_id={review.blog_id} shop='{review.shop_name}' validation={report.result.value}")
        return {"status": "success", "message": status_msg, "doc_id": doc_id, "validation": report.to_dict()}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/batch")
async def ingest_reviews_batch(reviews: List[ReviewDocument]):
    """Batch ingest with per-item validation"""
    try:
        retriever = get_retriever()
        results = []
        accepted = 0
        rejected = 0

        for review in reviews:
            report = _validator.validate(
                content=review.content, shop_id=review.shop_id,
                shop_name=review.shop_name, blog_id=review.blog_id,
            )
            if report.result.value == "REJECTED":
                rejected += 1
                results.append({"blog_id": review.blog_id, "status": "rejected", "issues": report.issues})
                continue

            doc_text = f"{review.title or ''}\n{review.content}".strip()
            metadata = {"blog_id": review.blog_id, "shop_id": review.shop_id,
                        "shop_name": review.shop_name, "user_name": review.user_name or "Anonymous"}
            retriever.add_document(f"blog_{review.blog_id}", doc_text, metadata)
            accepted += 1
            results.append({"blog_id": review.blog_id, "status": report.result.value.lower(),
                            "quality_score": round(report.quality_score, 4)})

        logger.info(f"Batch: {len(reviews)} total, {accepted} accepted, {rejected} rejected")
        return {"status": "success", "message": f"Ingested {accepted}/{len(reviews)} reviews",
                "total": len(reviews), "accepted": accepted, "rejected": rejected, "results": results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
