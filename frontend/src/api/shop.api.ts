import api from './index';
import type { ApiResponse, Shop, ShopType, PageResult } from '../types';

export const shopApi = {
  // Get shop by ID
  getShopById: async (id: number): Promise<ApiResponse<Shop>> => {
    const response = await api.get(`/shop/${id}`);
    return response.data;
  },

  // Get shops by type with pagination
  getShopsByType: async (
    typeId: number,
    current: number = 1,
    coords?: { x: number; y: number }
  ): Promise<ApiResponse<PageResult<Shop>>> => {
    const params: any = { typeId, current };
    if (coords) {
      params.x = coords.x;
      params.y = coords.y;
    }
    const response = await api.get('/shop/of/type', { params });
    return response.data;
  },

  // Search shops by name
  searchShops: async (
    name: string,
    current: number = 1
  ): Promise<ApiResponse<PageResult<Shop>>> => {
    const response = await api.get('/shop/of/name', {
      params: { name, current },
    });
    return response.data;
  },

  // Get all shop types
  getShopTypes: async (): Promise<ApiResponse<ShopType[]>> => {
    const response = await api.get('/shop-type/list');
    return response.data;
  },
};