// Common response wrapper
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  errorMsg?: string;
}

// User types
export interface User {
  id: number;
  nickName: string;
  icon: string;
  phone: string;
  createTime: string;
}

// Shop types
export interface Shop {
  id: number;
  name: string;
  typeId: number;
  images: string; // comma-separated URLs
  area: string;
  address: string;
  x: number; // longitude
  y: number; // latitude
  avgPrice: number;
  sold: number;
  comments: number;
  score: number;
  openHours: string;
}

export interface ShopType {
  id: number;
  name: string;
  icon: string;
  sort: number;
}

// Blog types
export interface Blog {
  id: number;
  shopId: number;
  userId: number;
  title: string;
  images: string;
  content: string;
  liked: number;
  comments: number;
  createTime: string;
  name: string; // user name
  icon: string; // user icon
  isLike: boolean;
}

// Coupon types
export interface Coupon {
  id: number;
  shopId: number;
  title: string;
  subTitle: string;
  rules: string;
  payValue: number;
  actualValue: number;
  type: number; // 0: regular, 1: flash sale
  status: number; // 1: available, 2: sold out, 3: expired
  stock: number;
  beginTime: string;
  endTime: string;
}

// Pagination wrapper
export interface PageResult<T> {
  records: T[];
  total: number;
  pages: number;
  current: number;
}