// src/pages/Home.tsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Coffee, Search } from 'lucide-react';
import { CategoryGrid } from '@/components/shop/CategoryGrid';
import { ShopCard } from '@/components/shop/ShopCard';
import { shopApi } from '@/api/shop.api';
import { blogApi } from '@/api/blog.api';
import { parseImages } from '@/utils/formatters';
import type { Shop, ShopType, Blog } from '@/types';

export const Home = () => {
	const navigate = useNavigate();

	const [shopTypes, setShopTypes] = useState<ShopType[]>([]);
	const [popularShops, setPopularShops] = useState<Shop[]>([]);
	const [featuredBlog, setFeaturedBlog] = useState<Blog | null>(null);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		const fetchHomeData = async () => {
			try {
				// Fetch shop types
				const typesResponse = await shopApi.getShopTypes();
				if (typesResponse.success) {
					setShopTypes(typesResponse.data || []);
				}

				// Fetch popular shops
				const shopsResponse = await shopApi.getShopsByType(1, 1);
				if (shopsResponse.success) {
					const records = shopsResponse.data?.records || shopsResponse.data || [];
					setPopularShops(records.slice(0, 3));
				}

				// Fetch featured blog (top liked post)
				const blogsResponse = await blogApi.getHotBlogs(1);
				if (blogsResponse.success) {
					const records = blogsResponse.data?.records || blogsResponse.data || []; 
					if (records.length > 0) {
						setFeaturedBlog(records[0]);
					}
				}
			} catch (error) {
				console.error('Failed to fetch home data:', error);
			} finally {
				setLoading(false);
			}
		};

		fetchHomeData();
	}, []);

	const handleShopClick = (shop: Shop) => {
		navigate(`/shop/${shop.id}`);
	};

	const handleCategoryClick = (typeId: number) => {
		navigate(`/explore?type=${typeId}`);
	};

	const handleSearchClick = () => {
		navigate('/explore');
	};

	const handleBannerClick = () => {
		if (featuredBlog) {
			navigate(`/blog/${featuredBlog.id}`);
		}
	};

	if (loading) {
		return (
			<div className="min-h-screen flex items-center justify-center">
				<div className="animate-spin text-primary-500">⏳</div>
			</div>
		);
	}

	const bannerImage = featuredBlog
		? parseImages(featuredBlog.images)[0]
		: "https://images.unsplash.com/photo-1571934811356-5cc55449d0f4?auto=format&fit=crop&q=80&w=800";

	return (
		<div className="pb-24">
			{/* Header */}
			<div className="sticky top-0 bg-white/95 backdrop-blur-sm z-40 px-4 py-3 flex justify-between items-center border-b border-gray-50">
				<div className="flex items-center gap-2">
					<div className="bg-primary-500 p-1.5 rounded-lg">
						<Coffee size={18} className="text-white" />
					</div>
					<span className="font-serif font-bold text-xl text-gray-800">TeaTime</span>
				</div>
				<div className="relative">
					<Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
					<input
						placeholder="Find tea..."
						onClick={handleSearchClick}
						readOnly
						className="bg-gray-100 rounded-full pl-9 pr-4 py-2 text-sm w-40 focus:w-48 transition-all focus:outline-none focus:ring-1 focus:ring-primary-500 cursor-pointer"
					/>
				</div>
			</div>

			{/* Hero Banner - Dynamic & Clickable */}
			<div
				onClick={handleBannerClick}
				className="m-4 rounded-2xl overflow-hidden relative h-48 shadow-lg cursor-pointer group"
			>
				<img
					src={bannerImage}
					alt={featuredBlog?.title || "Featured"}
					className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
				/>
				<div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent flex items-end p-6">
					<div>
						<span className="bg-secondary-500 text-white text-xs px-2 py-1 rounded-md font-medium mb-2 inline-block">
							{featuredBlog ? 'Featured Post' : 'Featured'}
						</span>
						<h2 className="text-white font-serif text-2xl font-bold line-clamp-1">
							{featuredBlog?.title || 'Discover Amazing Tea'}
						</h2>
						<p className="text-gray-200 text-sm mt-1 line-clamp-1">
							{featuredBlog ? `By ${featuredBlog.name} • ${featuredBlog.liked} likes` : 'Explore our community'}
						</p>
					</div>
				</div>
			</div>

			{/* Categories */}
			<div className="px-4 mb-8">
				<h3 className="font-serif font-bold text-lg mb-4 text-gray-800">Explore by Type</h3>
				<CategoryGrid
					categories={shopTypes.slice(0, 6)}
					onCategoryClick={handleCategoryClick}
				/>
			</div>

			{/* Popular Shops */}
			<div className="px-4">
				<div className="flex justify-between items-center mb-4">
					<h3 className="font-serif font-bold text-lg text-gray-800">Popular Near You</h3>
					<button
						onClick={() => navigate('/explore')}
						className="text-primary-500 text-sm font-medium hover:underline"
					>
						See All
					</button>
				</div>

				{popularShops.length > 0 ? (
					<div className="space-y-4">
						{popularShops.map(shop => (
							<ShopCard
								key={shop.id}
								shop={shop}
								onClick={() => handleShopClick(shop)}
							/>
						))}
					</div>
				) : (
					<div className="text-center py-8 text-gray-500">
						<p>No shops available yet</p>
					</div>
				)}
			</div>
		</div>
	);
};