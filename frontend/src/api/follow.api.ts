import api from './index';
import type { ApiResponse, User } from './types';

export const followApi = {
  // Follow/unfollow user
  toggleFollow: async (userId: number, isFollow: boolean): Promise<ApiResponse<null>> => {
    const response = await api.put(`/follow/${userId}/${isFollow}`);
    return response.data;
  },

  // Check if following a user
  isFollowing: async (userId: number): Promise<ApiResponse<boolean>> => {
    const response = await api.get(`/follow/or/not/${userId}`);
    return response.data;
  },

  // Get common follows
  getCommonFollows: async (userId: number): Promise<ApiResponse<User[]>> => {
    const response = await api.get(`/follow/common/${userId}`);
    return response.data;
  },

  // Get follower count
  getFollowerCount: async (userId: number): Promise<ApiResponse<number>> => {
    const response = await api.get(`/follow/followers/count/${userId}`);
    return response.data;
  },

  // Get following count
  getFollowingCount: async (userId: number): Promise<ApiResponse<number>> => {
    const response = await api.get(`/follow/following/count/${userId}`);
    return response.data;
  },
};