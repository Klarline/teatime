import { Star, MapPin } from 'lucide-react';
import type { Shop } from '@/types';
import { parseImages, formatRating, formatDistance, formatPrice } from '@/utils/formatters';

interface ShopCardProps {
	shop: Shop;
	onClick: () => void;
}

export const ShopCard = ({ shop, onClick }: ShopCardProps) => {
	const images = parseImages(shop.images);
	const firstImage = images[0] || '';

	return (
		<div
			onClick={onClick}
			className="bg-white rounded-xl p-3 shadow-sm border border-gray-100 flex gap-4 cursor-pointer active:scale-[0.99] transition-transform"
		>
			<img
				src={firstImage}
				alt={shop.name}
				className="w-24 h-24 rounded-lg object-cover bg-gray-100"
			/>

			<div className="flex-1 py-1">
				<div className="flex justify-between items-start">
					<h4 className="font-bold text-gray-900">{shop.name}</h4>
					<div className="flex items-center gap-1 bg-yellow-50 px-1.5 py-0.5 rounded text-yellow-700 text-xs font-bold">
						<Star size={10} fill="currentColor" />
						{formatRating(shop.score)}
					</div>
				</div>

				<p className="text-xs text-gray-500 mt-1 flex items-center gap-1">
					{shop.area}
				</p>

				{/* Distance - shows when available */}
				{shop.distance !== undefined && (
					<p className="text-xs text-green-600 font-medium mt-1 flex items-center gap-1">
						<MapPin size={10} />
						{formatDistance(shop.distance)} away
					</p>
				)}

				<div className="flex gap-2 mt-3">
					<span className="text-xs px-2 py-1 bg-gray-100 rounded text-gray-600">
						Avg {formatPrice(shop.avgPrice)}
					</span>
					<span className="text-xs px-2 py-1 bg-gray-100 rounded text-gray-600">
						{shop.comments} reviews
					</span>
				</div>
			</div>
		</div>
	);
};