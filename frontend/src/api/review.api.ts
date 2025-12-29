import api from './index';
import type { ApiResponse, Review, ReviewStats, CreateReviewRequest } from '../types';

export const reviewApi = {
  // Create a new review
  createReview: async (
    data: CreateReviewRequest
  ): Promise<ApiResponse<number>> => {
    const response = await api.post('/review', data);
    return response.data;
  },

  // Get reviews for a specific shop
  getShopReviews: async (
    shopId: number,
    current: number = 1,
    size: number = 10
  ): Promise<ApiResponse<Review[]>> => {
    const response = await api.get(`/review/shop/${shopId}`, {
      params: { current, size },
    });
    return response.data;
  },

  // Get shop rating statistics
  getShopRatingStats: async (
    shopId: number
  ): Promise<ApiResponse<ReviewStats>> => {
    const response = await api.get(`/review/stats/${shopId}`);
    return response.data;
  },

  // Check if current user has reviewed this shop
  hasUserReviewed: async (shopId: number): Promise<ApiResponse<boolean>> => {
    const response = await api.get(`/review/check/${shopId}`);
    return response.data;
  },

  // Delete a review
  deleteReview: async (reviewId: number): Promise<ApiResponse<void>> => {
    const response = await api.delete(`/review/${reviewId}`);
    return response.data;
  },
};