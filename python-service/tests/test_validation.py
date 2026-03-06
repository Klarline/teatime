"""Tests for review data validation module."""

import pytest
from app.rag.validation import ReviewValidator, ValidationResult


class TestReviewValidator:
    def setup_method(self):
        self.validator = ReviewValidator(min_content_length=20, min_quality_score=0.3)

    def test_accepts_quality_review(self):
        report = self.validator.validate(
            content="Amazing matcha latte with a cozy atmosphere. Friendly staff and reasonable prices. Perfect for studying.",
            shop_id=1, shop_name="Whisk Matcha", blog_id=100)
        assert report.result == ValidationResult.ACCEPTED
        assert report.quality_score > 0.3
        assert not report.is_duplicate
        assert len(report.issues) == 0

    def test_rejects_empty_content(self):
        report = self.validator.validate(content="", shop_id=1, shop_name="Test", blog_id=1)
        assert report.result == ValidationResult.REJECTED

    def test_rejects_very_short(self):
        report = self.validator.validate(content="Good tea", shop_id=1, shop_name="T", blog_id=1)
        assert report.result == ValidationResult.REJECTED

    def test_detects_duplicate(self):
        c = "This is a great tea shop with wonderful matcha lattes and cozy seating"
        r1 = self.validator.validate(content=c, shop_id=1, shop_name="A", blog_id=1)
        assert not r1.is_duplicate
        r2 = self.validator.validate(content=c, shop_id=2, shop_name="B", blog_id=2)
        assert r2.is_duplicate
        assert r2.result == ValidationResult.REJECTED

    def test_duplicate_case_insensitive(self):
        self.validator.validate(content="Great matcha latte here at this cafe", shop_id=1, shop_name="S", blog_id=1)
        r = self.validator.validate(content="GREAT MATCHA LATTE HERE AT THIS CAFE", shop_id=2, shop_name="S", blog_id=2)
        assert r.is_duplicate

    def test_flags_missing_metadata(self):
        report = self.validator.validate(
            content="Wonderful tea shop with great atmosphere and delicious drinks served here",
            shop_id=None, shop_name="Test", blog_id=1)
        assert not report.metadata_complete

    def test_flags_empty_shop_name(self):
        report = self.validator.validate(
            content="Great tea with wonderful flavors and friendly service staff always",
            shop_id=1, shop_name="", blog_id=1)
        assert not report.metadata_complete

    def test_quality_rewards_descriptive(self):
        r_good = self.validator.validate(
            content="Delicious matcha latte with amazing flavor. Cozy atmosphere, friendly staff, great quality. Recommend!",
            shop_id=1, shop_name="T", blog_id=1)
        v2 = ReviewValidator()
        r_generic = v2.validate(
            content="went there it was okay nothing special just a normal place to go sometimes",
            shop_id=2, shop_name="T2", blog_id=2)
        assert r_good.quality_score > r_generic.quality_score

    def test_spam_detection(self):
        r = self.validator.validate(content="GOOD GOOD GOOD GOOD GOOD GOOOOOOOD GREAAAAAT NIIIIICE",
            shop_id=1, shop_name="T", blog_id=1)
        assert r.quality_score < 0.5

    def test_stats_tracking(self):
        self.validator.validate(content="Wonderful tea shop with delicious matcha and great atmosphere",
            shop_id=1, shop_name="S", blog_id=1)
        self.validator.validate(content="", shop_id=2, shop_name="S", blog_id=2)
        stats = self.validator.get_stats()
        assert stats["total_processed"] == 2
        assert stats["rejected"] >= 1

    def test_report_to_dict(self):
        r = self.validator.validate(content="Great matcha cafe with wonderful atmosphere and delicious lattes",
            shop_id=1, shop_name="T", blog_id=1)
        d = r.to_dict()
        assert d["result"] in ["ACCEPTED", "FLAGGED", "REJECTED"]

    def test_acceptance_rate(self):
        for i in range(5):
            self.validator.validate(content=f"Wonderful tea shop number {i} with delicious drinks and great service here",
                shop_id=i, shop_name=f"Shop {i}", blog_id=i)
        self.validator.validate(content="", shop_id=99, shop_name="Bad", blog_id=99)
        stats = self.validator.get_stats()
        assert stats["total_processed"] == 6
        assert stats["acceptance_rate"] > 0
