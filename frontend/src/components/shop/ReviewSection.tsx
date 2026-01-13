import { useState, useEffect } from 'react';
import { Star, MessageSquare, X } from 'lucide-react';
import { ReviewCard } from './ReviewCard';
import { reviewApi } from '@/api/review.api';
import { useAuthStore } from '@/store/authStore';
import type { Review, ReviewStats, CreateReviewRequest } from '@/types';
import toast from 'react-hot-toast';

interface ReviewSectionProps {
  shopId: number;
}

export const ReviewSection = ({ shopId }: ReviewSectionProps) => {
  const { user, isAuthenticated } = useAuthStore();

  const [reviews, setReviews] = useState<Review[]>([]);
  const [stats, setStats] = useState<ReviewStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [hasReviewed, setHasReviewed] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const [currentPage, setCurrentPage] = useState(1);
  const [hasMore, setHasMore] = useState(false);

  // Form state
  const [rating, setRating] = useState(5);
  const [content, setContent] = useState('');

  useEffect(() => {
    fetchReviewData();
  }, [shopId]);

  const fetchReviewData = async () => {
    try {
      setLoading(true);

      // Fetch reviews
      const reviewsResponse = await reviewApi.getShopReviews(shopId, 1, 10);
      if (reviewsResponse.success) {
        // Append to existing reviews if page > 1
        if (currentPage === 1) {
          setReviews(reviewsResponse.data);
        } else {
          setReviews(prev => [...prev, ...reviewsResponse.data]);
        }
        // Check if there are more reviews
        setHasMore(reviewsResponse.data.length === 10);
      }

      // Only fetch stats on first page
      if (currentPage === 1) {
        const statsResponse = await reviewApi.getShopRatingStats(shopId);
        if (statsResponse.success) {
          setStats(statsResponse.data);
        }

        // Check if user has reviewed (only if authenticated)
        if (isAuthenticated) {
          const hasReviewedResponse = await reviewApi.hasUserReviewed(shopId);
          if (hasReviewedResponse.success) {
            setHasReviewed(hasReviewedResponse.data);
          }
        }
      }
    } catch (error) {
      console.error('Failed to fetch review data:', error);
    } finally {
      setLoading(false);
    }
  };

	const loadMoreReviews = () => {
    setCurrentPage(prev => prev + 1);
  };

  useEffect(() => {
    if (currentPage > 1) {
      fetchReviewData();
    }
  }, [currentPage]);

  const handleSubmit = async () => {
    if (!isAuthenticated) {
      toast.error('Please login to write a review');
      return;
    }

    if (!content.trim()) {
      toast.error('Please write your review');
      return;
    }

    try {
      setSubmitting(true);

      const reviewData: CreateReviewRequest = {
        shopId,
        rating,
        content: content.trim(),
      };

      const response = await reviewApi.createReview(reviewData);

      if (response.success) {
        toast.success('Review posted successfully!');
        setShowForm(false);
        setContent('');
        setRating(5);
        setHasReviewed(true);
        
        // Refresh reviews
        fetchReviewData();
      } else {
        toast.error(response.errorMsg || 'Failed to post review');
      }
    } catch (error: any) {
      console.error('Failed to create review:', error);
      toast.error(error.response?.data?.errorMsg || 'Failed to post review');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (reviewId: number) => {
    if (!confirm('Are you sure you want to delete this review?')) {
      return;
    }

    try {
      setDeletingId(reviewId);

      const response = await reviewApi.deleteReview(reviewId);

      if (response.success) {
        toast.success('Review deleted');
        setHasReviewed(false);
        
        // Remove from list
        setReviews(prev => prev.filter(r => r.id !== reviewId));
        
        // Refresh stats
        const statsResponse = await reviewApi.getShopRatingStats(shopId);
        if (statsResponse.success) {
          setStats(statsResponse.data);
        }
      } else {
        toast.error(response.errorMsg || 'Failed to delete review');
      }
    } catch (error: any) {
      console.error('Failed to delete review:', error);
      toast.error('Failed to delete review');
    } finally {
      setDeletingId(null);
    }
  };

  if (loading) {
    return (
      <div className="py-8 text-center text-gray-400">
        <div className="animate-spin">⏳</div>
      </div>
    );
  }

  return (
    <div className="mb-8">
      {/* Header with stats */}
      <div className="flex justify-between items-center mb-4">
        <h3 className="font-bold text-lg flex items-center gap-2">
          <MessageSquare size={20} className="text-primary-500" />
          Reviews
          {stats && stats.reviewCount > 0 && (
            <span className="text-sm text-gray-400">({stats.reviewCount})</span>
          )}
        </h3>

        {stats && stats.reviewCount > 0 && (
          <div className="flex items-center gap-1 bg-yellow-50 px-3 py-1 rounded-full">
            <Star size={14} className="text-yellow-400 fill-yellow-400" />
            <span className="font-bold text-sm">{stats.avgRating.toFixed(1)}</span>
          </div>
        )}
      </div>

      {/* Write Review Button */}
      {isAuthenticated && !hasReviewed && !showForm && (
        <button
          onClick={() => setShowForm(true)}
          className="w-full mb-4 py-3 border-2 border-dashed border-gray-200 rounded-lg text-sm text-gray-500 hover:border-primary-300 hover:text-primary-500 transition-colors"
        >
          + Write a Review
        </button>
      )}

      {/* Review Form */}
      {showForm && (
        <div className="mb-6 p-4 border border-gray-200 rounded-lg bg-gray-50">
          <div className="flex justify-between items-center mb-3">
            <span className="font-semibold text-sm">Write Your Review</span>
            <button
              onClick={() => setShowForm(false)}
              className="text-gray-400 hover:text-gray-600"
            >
              <X size={20} />
            </button>
          </div>

          {/* Star Rating Input */}
          <div className="mb-3">
            <div className="flex items-center gap-2 mb-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  onClick={() => setRating(star)}
                  className="transition-transform hover:scale-110"
                >
                  <Star
                    size={28}
                    className={
                      star <= rating
                        ? 'text-yellow-400 fill-yellow-400'
                        : 'text-gray-300'
                    }
                  />
                </button>
              ))}
            </div>
          </div>

          {/* Content Input */}
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Share your experience..."
            className="w-full p-3 border border-gray-200 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            rows={4}
          />

          {/* Submit Button */}
          <button
            onClick={handleSubmit}
            disabled={submitting || !content.trim()}
            className="w-full mt-3 py-2.5 bg-primary-500 text-white rounded-lg font-semibold text-sm hover:bg-primary-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
          >
            {submitting ? 'Posting...' : 'Post Review'}
          </button>
        </div>
      )}

      {/* Reviews List */}
      {reviews.length > 0 ? (
        <div className="space-y-4">
          {reviews.map((review) => (
            <ReviewCard
              key={review.id}
              review={review}
              currentUserId={user?.id}
              onDelete={handleDelete}
              isDeleting={deletingId === review.id}
            />
          ))}
          
          {/* Load More Button */}
          {hasMore && (
            <button
              onClick={loadMoreReviews}
              className="w-full py-3 border border-gray-200 rounded-lg text-sm text-gray-600 hover:bg-gray-50 transition-colors"
            >
              Load More Reviews
            </button>
          )}
        </div>
      ) : (
        <div className="text-center py-8 text-gray-400 text-sm">
          No reviews yet. Be the first to review!
        </div>
      )}
    </div>
  );
};