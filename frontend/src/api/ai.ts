import axios from './index';

export interface RecommendationRequest {
  query: string;
  maxResults?: number;
}

export interface RecommendationResponse {
  recommendations: string;
  sourceBlogs: number[];
}

export interface AIHealthResponse {
  status: string;
  vectorDbCount: number;
}

export const aiApi = {
  getRecommendations: async (request: RecommendationRequest): Promise<RecommendationResponse> => {
    const response = await axios.post('/ai/recommend', request, {
      timeout: 30000
    });
    return response.data.data;
  },

  checkHealth: async (): Promise<AIHealthResponse> => {
    const response = await axios.get('/ai/health');
    return response.data.data;
  }
};