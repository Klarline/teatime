import api from './index';
import type { ApiResponse } from './types';

export const uploadApi = {
  // Upload blog image
  uploadBlogImage: async (file: File): Promise<ApiResponse<string>> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await api.post('/upload/blog', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};