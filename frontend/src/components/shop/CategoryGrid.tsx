import type { ShopType } from '@/api/types';

interface CategoryGridProps {
  categories: ShopType[];
  onCategoryClick: (categoryId: number) => void;
}

export const CategoryGrid = ({ categories, onCategoryClick }: CategoryGridProps) => {
  return (
    <div className="grid grid-cols-3 gap-3">
      {categories.map((type) => (
        <button 
          key={type.id} 
          onClick={() => onCategoryClick(type.id)}
          className="flex flex-col items-center justify-center p-3 bg-white border border-gray-100 rounded-xl shadow-sm hover:border-primary-500 hover:shadow-md transition-all group"
        >
          <span className="text-2xl mb-1 group-hover:scale-110 transition-transform">
            {type.icon}
          </span>
          <span className="text-xs font-medium text-gray-600 group-hover:text-primary-500">
            {type.name}
          </span>
        </button>
      ))}
    </div>
  );
};