import { ReviewSection } from '@/components/shop/ReviewSection';
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
	ChevronLeft, Share2, MoreHorizontal, MapPin,
	Clock, DollarSign, Star, Coffee
} from 'lucide-react';
import { shopApi } from '@/api/shop.api';
import { couponApi } from '@/api/coupon.api';
import { parseImages, formatPrice, formatRating } from '@/utils/formatters';
import { useAuthStore } from '@/store/authStore';
import type { Shop, Coupon } from '@/types';

export const ShopDetail = () => {
	const { id } = useParams<{ id: string }>();
	const navigate = useNavigate();
	const { isAuthenticated } = useAuthStore();

	const [shop, setShop] = useState<Shop | null>(null);
	const [coupons, setCoupons] = useState<Coupon[]>([]);
	const [loading, setLoading] = useState(true);
	const [currentImageIndex, setCurrentImageIndex] = useState(0);
	const [purchasingCouponId, setPurchasingCouponId] = useState<number | null>(null);

	useEffect(() => {
		const fetchShopData = async () => {
			if (!id) return;

			try {
				setLoading(true);

				// Fetch shop details
				const shopResponse = await shopApi.getShopById(parseInt(id));
				if (shopResponse.success) {
					setShop(shopResponse.data);
				}

				// Fetch coupons
				const couponsResponse = await couponApi.getCouponsByShopId(parseInt(id));
				if (couponsResponse.success) {
					setCoupons(couponsResponse.data || []);
				}
			} catch (error) {
				console.error('Failed to fetch shop data:', error);
			} finally {
				setLoading(false);
			}
		};

		fetchShopData();
	}, [id]);

	const handlePurchaseCoupon = async (coupon: Coupon) => {
		// Check if user is logged in
		if (!isAuthenticated) {
			// Show toast or redirect to login
			alert('Please login to claim coupons');
			navigate('/login');
			return;
		}

		// Check if it's a flash sale (type = 1)
		if (coupon.type !== 1) {
			// Regular coupons might just be informational
			alert('This coupon can be used directly at the shop!');
			return;
		}

		// Purchase flash sale coupon
		try {
			setPurchasingCouponId(coupon.id);

			const response = await couponApi.purchaseFlashSale(coupon.id);

			if (response.success) {
				// Success!
				alert(`Successfully claimed ${coupon.title}!`);
				
				// Optional: Remove from list or mark as claimed
				setCoupons(prev => 
					prev.map(c => 
						c.id === coupon.id 
							? { ...c, status: 2 } // Mark as sold out/claimed
							: c
					)
				);
			} else {
				// Handle specific errors
				alert(response.errorMsg || 'Failed to claim coupon');
			}
		} catch (error: any) {
			console.error('Failed to purchase coupon:', error);
			
			// Handle common errors
			if (error.response?.status === 401) {
				alert('Please login to claim coupons');
				navigate('/login');
			} else {
				alert(error.response?.data?.errorMsg || 'Failed to claim coupon. Please try again.');
			}
		} finally {
			setPurchasingCouponId(null);
		}
	};

	const handleBack = () => {
		navigate(-1);
	};

	const handleShare = () => {
		if (navigator.share && shop) {
			navigator.share({
				title: shop.name,
				text: `Check out ${shop.name} on TeaTime!`,
				url: window.location.href,
			});
		}
	};

	if (loading) {
		return (
			<div className="min-h-screen flex items-center justify-center">
				<div className="animate-spin text-primary-500">⏳</div>
			</div>
		);
	}

	if (!shop) {
		return (
			<div className="min-h-screen flex flex-col items-center justify-center p-8">
				<p className="text-gray-500 mb-4">Shop not found</p>
				<button
					onClick={handleBack}
					className="text-primary-500 underline"
				>
					Go Back
				</button>
			</div>
		);
	}

	const images = parseImages(shop.images);

	return (
		<div className="bg-white min-h-screen pb-24">
			{/* Header Navigation */}
			<div className="fixed top-0 left-0 right-0 z-50 flex justify-between items-center p-4 bg-gradient-to-b from-black/50 to-transparent pointer-events-none">
				<button
					onClick={handleBack}
					className="p-2 bg-white/20 backdrop-blur rounded-full text-white pointer-events-auto hover:bg-white/30 transition-colors"
				>
					<ChevronLeft size={24} />
				</button>
				<div className="flex gap-3 pointer-events-auto">
					<button
						onClick={handleShare}
						className="p-2 bg-white/20 backdrop-blur rounded-full text-white hover:bg-white/30 transition-colors"
					>
						<Share2 size={20} />
					</button>
					<button className="p-2 bg-white/20 backdrop-blur rounded-full text-white hover:bg-white/30 transition-colors">
						<MoreHorizontal size={20} />
					</button>
				</div>
			</div>

			{/* Image Gallery */}
			<div className="h-72 relative bg-gray-200">
				<img
					src={images[currentImageIndex]}
					alt={shop.name}
					className="w-full h-full object-cover"
				/>
				<div className="absolute bottom-4 right-4 bg-black/60 text-white text-xs px-2 py-1 rounded-md backdrop-blur-sm">
					{currentImageIndex + 1}/{images.length} Photos
				</div>

				{/* Image Navigation Dots */}
				{images.length > 1 && (
					<div className="absolute bottom-4 left-0 right-0 flex justify-center gap-2">
						{images.map((_, index) => (
							<button
								key={index}
								onClick={() => setCurrentImageIndex(index)}
								className={`w-2 h-2 rounded-full transition-colors ${index === currentImageIndex ? 'bg-white' : 'bg-white/50'
									}`}
							/>
						))}
					</div>
				)}
			</div>

			{/* Shop Information */}
			<div className="relative -mt-6 bg-white rounded-t-3xl px-6 py-8 shadow-inner">
				{/* Title & Rating */}
				<div className="flex justify-between items-start mb-2">
					<h1 className="text-2xl font-bold text-gray-900 w-3/4 leading-tight">
						{shop.name}
					</h1>
					<div className="flex flex-col items-center bg-gray-50 px-3 py-1 rounded-lg border border-gray-100">
						<div className="flex items-center gap-1 text-yellow-500 font-bold">
							<Star size={14} fill="currentColor" />
							<span>{formatRating(shop.score)}</span>
						</div>
						<span className="text-[10px] text-gray-400">{shop.comments}+ reviews</span>
					</div>
				</div>

				{/* Details */}
				<div className="space-y-3 border-b border-gray-100 pb-6">
					<div className="flex items-center gap-3 text-sm text-gray-700">
						<div className="w-8 flex justify-center">
							<Coffee size={18} className="text-primary-500" />
						</div>
						<span className="font-medium">{shop.area}</span>
					</div>
					<div className="flex items-center gap-3 text-sm text-gray-700">
						<div className="w-8 flex justify-center">
							<MapPin size={18} className="text-primary-500" />
						</div>
						<span>{shop.address}</span>
					</div>
					<div className="flex items-center gap-3 text-sm text-gray-700">
						<div className="w-8 flex justify-center">
							<Clock size={18} className="text-primary-500" />
						</div>
						<span>{shop.openHours}</span>
					</div>
					<div className="flex items-center gap-3 text-sm text-gray-700">
						<div className="w-8 flex justify-center">
							<DollarSign size={18} className="text-primary-500" />
						</div>
						<span>{formatPrice(shop.avgPrice)} / person</span>
					</div>
				</div>

				{/* Stats */}
				<div className="pt-6 pb-6">
					<div className="grid grid-cols-3 gap-4 text-center">
						<div>
							<div className="text-2xl font-bold text-gray-900">{shop.sold}</div>
							<div className="text-xs text-gray-500">Orders</div>
						</div>
						<div>
							<div className="text-2xl font-bold text-gray-900">{shop.comments}</div>
							<div className="text-xs text-gray-500">Reviews</div>
						</div>
						<div>
							<div className="text-2xl font-bold text-gray-900">{formatRating(shop.score)}</div>
							<div className="text-xs text-gray-500">Rating</div>
						</div>
					</div>
				</div>

				{/* Coupons Section */}
				{coupons.length > 0 && (
					<div className="mb-8">
						<div className="flex justify-between items-end mb-3">
							<h3 className="font-bold text-lg">Offers & Coupons</h3>
						</div>

						<div className="space-y-3">
							{coupons.slice(0, 3).map(coupon => (
								<div
									key={coupon.id}
									className="flex border border-gray-100 rounded-lg overflow-hidden shadow-sm h-24 relative"
								>
									{/* Left side - Discount */}
									<div className="w-24 bg-secondary-500 flex flex-col items-center justify-center text-white p-2 text-center">
										<span className="text-xs opacity-80">
											{coupon.type === 1 ? 'FLASH' : 'SAVE'}
										</span>
										<span className="text-2xl font-bold">
											{formatPrice(coupon.actualValue)}
										</span>
									</div>

									{/* Right side - Details */}
									<div className="flex-1 p-3 flex flex-col justify-between bg-white relative">
										<div className="absolute left-[-6px] top-1/2 -translate-y-1/2 w-3 h-3 bg-white rounded-full border-r border-gray-100 z-10"></div>
										<div className="border-l-2 border-dashed border-gray-200 h-full absolute left-0 top-0"></div>

										<div>
											<h4 className="font-bold text-gray-800 text-sm truncate">{coupon.title}</h4>
											<p className="text-xs text-gray-500 truncate">{coupon.subTitle}</p>
										</div>
										<div className="flex justify-between items-center">
											<span className="text-[10px] text-gray-400">
												Pay {formatPrice(coupon.payValue)}
											</span>
											<button
												onClick={() => handlePurchaseCoupon(coupon)}
												className={`px-3 py-1 rounded-full text-xs font-bold transition-all ${
													coupon.status === 1
														? purchasingCouponId === coupon.id
															? 'bg-gray-100 text-gray-400 cursor-wait'
															: 'bg-red-50 text-red-500 hover:bg-red-100 active:scale-95'
														: 'bg-gray-100 text-gray-400 cursor-not-allowed'
												}`}
												disabled={coupon.status !== 1 || purchasingCouponId === coupon.id}
											>
												{purchasingCouponId === coupon.id 
													? 'Claiming...' 
													: coupon.status === 1 
														? 'Claim' 
														: coupon.status === 2 
															? 'Sold Out' 
															: 'Expired'}
											</button>
										</div>
									</div>
								</div>
							))}
						</div>
					</div>
				)}

				{/* Reviews Section */}
				<ReviewSection shopId={parseInt(id!)} />
				
			</div>
		</div>
	);
};