"""
Review Data Validation Module

Quality control on incoming review data before embedding into vector DB.
Prevents low-quality, duplicate, or spam content from degrading retrieval.
"""

import hashlib
import logging
import math
import re
from dataclasses import dataclass
from typing import Optional, Set, Dict, List
from enum import Enum

logger = logging.getLogger(__name__)


class ValidationResult(str, Enum):
    ACCEPTED = "ACCEPTED"
    FLAGGED = "FLAGGED"
    REJECTED = "REJECTED"


@dataclass
class ValidationReport:
    result: ValidationResult
    content_length: int
    quality_score: float
    is_duplicate: bool
    metadata_complete: bool
    issues: List[str]

    def to_dict(self) -> Dict:
        return {
            "result": self.result.value, "content_length": self.content_length,
            "quality_score": round(self.quality_score, 4), "is_duplicate": self.is_duplicate,
            "metadata_complete": self.metadata_complete, "issues": self.issues,
        }


class ReviewValidator:
    """Validates review content quality before vector database ingestion."""

    def __init__(self, min_content_length=20, min_quality_score=0.3, max_duplicate_window=1000):
        self.min_content_length = min_content_length
        self.min_quality_score = min_quality_score
        self._content_hashes: Set[str] = set()
        self._max_hashes = max_duplicate_window
        self.stats = {"total_processed": 0, "accepted": 0, "flagged": 0, "rejected": 0, "duplicates_caught": 0}

    def validate(self, content, shop_id=None, shop_name=None, blog_id=None):
        self.stats["total_processed"] += 1
        issues = []
        content_length = len(content.strip()) if content else 0
        if content_length < self.min_content_length:
            issues.append(f"Content too short ({content_length} chars, minimum {self.min_content_length})")

        is_duplicate = self._check_duplicate(content)
        if is_duplicate:
            issues.append("Duplicate content detected")
            self.stats["duplicates_caught"] += 1

        quality_score = self._score_content_quality(content)
        if quality_score < self.min_quality_score:
            issues.append(f"Low content quality ({quality_score:.2f}, minimum {self.min_quality_score})")

        metadata_complete = all([shop_id is not None, shop_name and len(shop_name.strip()) > 0, blog_id is not None])
        if not metadata_complete:
            missing = []
            if shop_id is None: missing.append("shop_id")
            if not shop_name or not shop_name.strip(): missing.append("shop_name")
            if blog_id is None: missing.append("blog_id")
            issues.append(f"Missing metadata: {', '.join(missing)}")

        result = self._determine_result(content_length, quality_score, is_duplicate, metadata_complete, issues)
        self.stats[result.value.lower()] += 1

        report = ValidationReport(result=result, content_length=content_length, quality_score=quality_score,
                                   is_duplicate=is_duplicate, metadata_complete=metadata_complete, issues=issues)
        logger.info(f"Validation: blog_id={blog_id} shop='{shop_name}' result={result.value} quality={quality_score:.2f}")
        return report

    def _check_duplicate(self, content):
        if not content: return False
        h = hashlib.md5(content.strip().lower().encode()).hexdigest()
        if h in self._content_hashes: return True
        self._content_hashes.add(h)
        if len(self._content_hashes) > self._max_hashes:
            self._content_hashes = set(list(self._content_hashes)[len(self._content_hashes)//2:])
        return False

    def _score_content_quality(self, content):
        if not content or len(content.strip()) == 0: return 0.0
        text = content.strip()
        words = text.split()
        word_count = len(words)
        length_score = min(math.log(max(word_count, 1) + 1) / math.log(101), 1.0)
        unique_words = set(w.lower() for w in words if w.isalpha())
        diversity_score = len(unique_words) / word_count if word_count > 0 else 0.0
        descriptive_terms = {"delicious","cozy","quiet","atmosphere","friendly","matcha","latte","bubble",
            "flavor","recommend","perfect","fresh","organic","traditional","modern","service","staff",
            "price","quality","experience","amazing","great","love","best","favorite","peaceful",
            "relaxing","spacious","clean","warm"}
        desc_matches = sum(1 for w in unique_words if w in descriptive_terms)
        descriptive_score = min(desc_matches / 3.0, 1.0)
        spam_penalty = 0.0
        if re.search(r'(.)\1{4,}', text): spam_penalty += 0.3
        if word_count > 3 and sum(1 for w in words if w.isupper()) / word_count > 0.5: spam_penalty += 0.2
        if word_count > 5 and len(unique_words) < word_count * 0.3: spam_penalty += 0.3
        return max(0.0, min(1.0, 0.30*length_score + 0.25*diversity_score + 0.25*descriptive_score + 0.20*(1.0-spam_penalty)))

    def _determine_result(self, content_length, quality_score, is_duplicate, metadata_complete, issues):
        if is_duplicate: return ValidationResult.REJECTED
        if content_length < 10: return ValidationResult.REJECTED
        if quality_score < 0.1: return ValidationResult.REJECTED
        if not metadata_complete: return ValidationResult.FLAGGED
        if content_length < self.min_content_length: return ValidationResult.FLAGGED
        if quality_score < self.min_quality_score: return ValidationResult.FLAGGED
        return ValidationResult.ACCEPTED if not issues else ValidationResult.FLAGGED

    def get_stats(self):
        total = self.stats["total_processed"]
        return {**self.stats,
            "acceptance_rate": round(self.stats["accepted"] / total, 4) if total > 0 else 0.0,
            "rejection_rate": round(self.stats["rejected"] / total, 4) if total > 0 else 0.0,
        }
