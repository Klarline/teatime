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
  name: string;      // Author name
  icon: string;      // Author icon
  isLike: boolean;   // Current user liked?
}

export interface ShopType {
  id: number;
  name: string;
  icon: string;
  sort: number;
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