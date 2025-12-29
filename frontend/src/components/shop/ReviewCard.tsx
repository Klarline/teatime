import { Star, Trash2 } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import type { Review } from '@/types';

interface ReviewCardProps {
  review: Review;
  currentUserId?: number;
  onDelete?: (reviewId: number) => void;
  isDeleting?: boolean;
}

export const ReviewCard = ({ review, currentUserId, onDelete, isDeleting }: ReviewCardProps) => {
  const isOwner = currentUserId === review.userId;

  return (
    <div className="border-b border-gray-100 pb-4 last:border-b-0">
      {/* User info & rating */}
      <div className="flex items-start justify-between mb-2">
        <div className="flex items-center gap-3">
          <img
            src={review.icon || '/default-avatar.png'}
            alt={review.nickName}
            className="w-10 h-10 rounded-full object-cover"
          />
          <div>
            <div className="font-semibold text-gray-900 text-sm">
              {review.nickName || 'Anonymous'}
            </div>
            <div className="flex items-center gap-1 mt-0.5">
              {[1, 2, 3, 4, 5].map((star) => (
                <Star
                  key={star}
                  size={12}
                  className={
                    star <= review.rating
                      ? 'text-yellow-400 fill-yellow-400'
                      : 'text-gray-300'
                  }
                />
              ))}
            </div>
          </div>
        </div>

        {/* Delete button for owner */}
        {isOwner && onDelete && (
          <button
            onClick={() => onDelete(review.id)}
            disabled={isDeleting}
            className="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
          >
            <Trash2 size={16} />
          </button>
        )}
      </div>

      {/* Review content */}
      <p className="text-sm text-gray-700 leading-relaxed mb-2">
        {review.content}
      </p>

      {/* Timestamp */}
      <div className="text-xs text-gray-400">
        {formatDistanceToNow(new Date(review.createTime), { addSuffix: true })}
      </div>
    </div>
  );
};