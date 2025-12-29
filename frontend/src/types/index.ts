// ============================================
// API Response Wrappers
// ============================================

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  errorMsg?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  pages: number;
  current: number;
}

// ============================================
// Domain Types
// ============================================

export interface User {
  id: number;
  nickName: string;
  icon: string;
  phone: string;
  createTime?: string;
}

export interface Shop {
  id: number;
  name: string;
  typeId: number;
  images: string;
  area: string;
  address: string;
  x: number;
  y: number;
  avgPrice: number;
  sold: number;
  comments: number;
  score: number;
  openHours: string;
  distance?: number;
}

export interface ShopType {
  id: number;
  name: string;
  icon: string;
  sort: number;
}

export interface Blog {
  id: number;
  shopId?: number;
  userId: number;
  title: string;
  images: string;
  content: string;
  liked: number;
  comments: number;
  createTime: string;
  name: string;
  icon: string;
  isLike: boolean;
}

export interface Coupon {
  id: number;
  shopId: number;
  title: string;
  subTitle: string;
  rules: string;
  payValue: number;
  actualValue: number;
  type: number;
  status: number;
  stock: number;
  beginTime: string;
  endTime: string;
}

export interface Review {
  id: number;
  shopId: number;
  userId: number;
  rating: number;
  content: string;
  createTime: string;
  updateTime: string;
  nickName?: string;
  icon?: string;
}

export interface ReviewStats {
  avgRating: number;
  reviewCount: number;
}

export interface CreateReviewRequest {
  shopId: number;
  rating: number;
  content: string;
}