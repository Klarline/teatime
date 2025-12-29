import api from './index';
import type { ApiResponse, Blog, User, PageResult } from '../types';

export const blogApi = {
  // Create blog post
  createBlog: async (data: {
    title: string;
    images: string;
    content: string;
    shopId?: number;
  }): Promise<ApiResponse<number>> => {
    const response = await api.post('/blog', data);
    return response.data;
  },

  // Get blog by ID
  getBlogById: async (id: number): Promise<ApiResponse<Blog>> => {
    const response = await api.get(`/blog/${id}`);
    return response.data;
  },

  // Get hot blogs
  getHotBlogs: async (current: number = 1): Promise<ApiResponse<PageResult<Blog>>> => {
    const response = await api.get('/blog/hot', { params: { current } });
    return response.data;
  },

  // Get my blogs
  getMyBlogs: async (current: number = 1): Promise<ApiResponse<PageResult<Blog>>> => {
    const response = await api.get('/blog/of/me', { params: { current } });
    return response.data;
  },

  // Get blogs by user ID
  getBlogsByUserId: async (
    userId: number,
    current: number = 1
  ): Promise<ApiResponse<PageResult<Blog>>> => {
    const response = await api.get('/blog', { params: { id: userId, current } });
    return response.data;
  },

  // Like/unlike blog
  likeBlog: async (id: number): Promise<ApiResponse<null>> => {
    const response = await api.put(`/blog/like/${id}`);
    return response.data;
  },

  // Get users who liked the blog
  getBlogLikes: async (id: number): Promise<ApiResponse<User[]>> => {
    const response = await api.get(`/blog/likes/${id}`);
    return response.data;
  },
};