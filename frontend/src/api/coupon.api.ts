import api from './index';
import type { ApiResponse, Coupon } from './types';

export const couponApi = {
  // Get coupons for a shop
  getCouponsByShopId: async (shopId: number): Promise<ApiResponse<Coupon[]>> => {
    const response = await api.get(`/coupon/list/${shopId}`);
    return response.data;
  },

  // Purchase flash sale coupon
  purchaseFlashSale: async (couponId: number): Promise<ApiResponse<number>> => {
    const response = await api.post(`/coupon-order/flash-sale/${couponId}`);
    return response.data;
  },
};