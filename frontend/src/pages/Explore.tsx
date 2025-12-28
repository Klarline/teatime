import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Search, MapPin } from 'lucide-react';
import { ShopCard } from '@/components/shop/ShopCard';
import { shopApi } from '@/api/shop.api';
import { useGeolocation, useDebounce } from '@/hooks';
import type { Shop, ShopType } from '@/api/types';

export const Explore = () => {
	const navigate = useNavigate();
	const [searchParams] = useSearchParams();

	// Geolocation
	const { coords, loading: geoLoading, error: geoError, getLocation } = useGeolocation();

	// State
	const [shopTypes, setShopTypes] = useState<ShopType[]>([]);
	const [activeTab, setActiveTab] = useState<number>(0);
	const [shops, setShops] = useState<Shop[]>([]);
	const [searchQuery, setSearchQuery] = useState('');
	const [loading, setLoading] = useState(true);
	const [page, setPage] = useState(1);
	const [hasMore, setHasMore] = useState(true);

	// Debounce search to avoid excessive API calls
	const debouncedSearch = useDebounce(searchQuery, 500);

	// Get location on mount
	useEffect(() => {
		getLocation();
	}, [getLocation]);

	// Handle URL type parameter
	useEffect(() => {
		const typeParam = searchParams.get('type');
		if (typeParam) {
			setActiveTab(parseInt(typeParam));
		}
	}, [searchParams]);

	// Fetch shop types
	useEffect(() => {
		const fetchShopTypes = async () => {
			try {
				const response = await shopApi.getShopTypes();
				if (response.success) {
					setShopTypes(response.data || []);
				}
			} catch (error) {
				console.error('Failed to fetch shop types:', error);
			}
		};

		fetchShopTypes();
	}, []);

	// Fetch shops (with geolocation and debounced search)
	useEffect(() => {
		const fetchShops = async () => {
			setLoading(true);
			try {
				let allShops: Shop[] = [];

				if (debouncedSearch.trim()) {
					// Search by name
					const response = await shopApi.searchShops(debouncedSearch, 1);
					if (response.success) {
						allShops = response.data?.records || response.data || [];
					}
				} else if (activeTab === 0) {
					// "All" tab - fetch ALL types and combine
					const typePromises = shopTypes.map(type =>
						shopApi.getShopsByType(type.id, 1, coords || undefined)
					);

					const responses = await Promise.all(typePromises);
					responses.forEach(response => {
						if (response.success) {
							const records = response.data?.records || response.data || [];
							allShops.push(...records);
						}
					});
				} else {
					// Specific category
					const response = await shopApi.getShopsByType(
						activeTab,
						1,
						coords || undefined
					);

					if (response.success) {
						allShops = response.data?.records || response.data || [];
					}
				}

				setShops(allShops);
				setHasMore(false);
				setPage(1);
			} catch (error) {
				console.error('Failed to fetch shops:', error);
				setShops([]);
			} finally {
				setLoading(false);
			}
		};

		// Only fetch if shopTypes are loaded (avoid empty array on first render)
		if (shopTypes.length > 0 || debouncedSearch.trim() || activeTab !== 0) {
			fetchShops();
		}
	}, [activeTab, debouncedSearch, coords, shopTypes]);

	const handleTabClick = (typeId: number) => {
		setActiveTab(typeId);
		setSearchQuery('');
	};

	const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		setSearchQuery(e.target.value);
	};

	const handleShopClick = (shop: Shop) => {
		navigate(`/shop/${shop.id}`);
	};

	const loadMore = async () => {
		if (!hasMore || loading) return;

		try {
			const nextPage = page + 1;
			let response;

			if (debouncedSearch.trim()) {
				response = await shopApi.searchShops(debouncedSearch, nextPage);
			} else if (activeTab === 0) {
				response = await shopApi.getShopsByType(
					1,
					nextPage,
					coords || undefined
				);
			} else {
				response = await shopApi.getShopsByType(
					activeTab,
					nextPage,
					coords || undefined
				);
			}

			if (response.success) {
				const records = response.data?.records || response.data || [];
				setShops([...shops, ...records]);
				setHasMore(response.data?.current < response.data?.pages);
				setPage(nextPage);
			}
		} catch (error) {
			console.error('Failed to load more shops:', error);
		}
	};

	return (
		<div className="pb-24 h-full flex flex-col">
			{/* Header with Search */}
			<div className="bg-white px-4 py-3 border-b border-gray-100 sticky top-0 z-10">
				{/* Search Input */}
				<div className="relative mb-3">
					<Search className="absolute left-3 top-3 text-gray-400" size={18} />
					<input
						placeholder="Search shops, teas..."
						value={searchQuery}
						onChange={handleSearchChange}
						className="w-full bg-gray-100 rounded-xl pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/50"
					/>
				</div>

				{/* Location Status Indicator */}
				{geoLoading && (
					<div className="text-xs text-gray-500 mb-2 flex items-center gap-1.5">
						<div className="animate-spin">⏳</div>
						<span>Getting your location...</span>
					</div>
				)}
				{coords && !debouncedSearch && (
					<div className="text-xs text-green-600 mb-2 flex items-center gap-1.5">
						<MapPin size={12} />
						<span>Showing shops near you</span>
					</div>
				)}
				{geoError && !debouncedSearch && (
					<div className="text-xs text-amber-600 mb-2 flex items-center gap-1.5">
						<MapPin size={12} />
						<span>Enable location for nearby shops</span>
						<button
							onClick={getLocation}
							className="underline hover:text-amber-700 ml-1"
						>
							Try again
						</button>
					</div>
				)}

				{/* Category Tabs */}
				<div className="flex gap-4 overflow-x-auto no-scrollbar pb-1">
					<button
						onClick={() => handleTabClick(0)}
						className={`whitespace-nowrap px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${activeTab === 0
								? 'bg-primary-500 text-white'
								: 'text-gray-600 bg-gray-100'
							}`}
					>
						All
					</button>
					{shopTypes.map(type => (
						<button
							key={type.id}
							onClick={() => handleTabClick(type.id)}
							className={`whitespace-nowrap px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${activeTab === type.id
									? 'bg-primary-500 text-white'
									: 'text-gray-600 bg-gray-100'
								}`}
						>
							{type.icon} {type.name}
						</button>
					))}
				</div>
			</div>

			{/* Shop List */}
			<div className="p-4 space-y-4 overflow-y-auto flex-1">
				{loading && shops.length === 0 ? (
					<div className="flex justify-center items-center h-64">
						<div className="animate-spin text-primary-500">⏳</div>
					</div>
				) : shops.length > 0 ? (
					<>
						{shops.map(shop => (
							<div key={shop.id} onClick={() => handleShopClick(shop)} className="cursor-pointer">
								<ShopCard shop={shop} onClick={() => handleShopClick(shop)} />
							</div>
						))}

						{/* Load More Button */}
						{hasMore && (
							<div className="flex justify-center py-4">
								<button
									onClick={loadMore}
									disabled={loading}
									className="px-6 py-2 bg-primary-500 text-white rounded-full font-medium hover:bg-primary-600 transition-colors disabled:opacity-50"
								>
									{loading ? 'Loading...' : 'Load More'}
								</button>
							</div>
						)}
					</>
				) : (
					<div className="text-center py-16 text-gray-500">
						<p>No shops found</p>
						{debouncedSearch && (
							<p className="text-sm mt-2">Try a different search term</p>
						)}
					</div>
				)}
			</div>
		</div>
	);
};