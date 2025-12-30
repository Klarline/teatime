import { Heart } from 'lucide-react';
import { parseImages } from '@/utils/formatters';
import type { Blog } from '@/types';

interface BlogCardProps {
  blog: Blog;
  onClick: () => void;
  onLike: () => void;
}

export const BlogCard = ({ blog, onClick, onLike }: BlogCardProps) => {
  const images = parseImages(blog.images);
  const firstImage = images[0];

  return (
    <div 
      className="break-inside-avoid mb-2 bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow cursor-pointer"
    >
      {/* Image */}
      <div className="relative" onClick={onClick}>
        <img 
          src={firstImage} 
          alt={blog.title}
          className="w-full h-auto object-cover" 
        />
      </div>

      {/* Content */}
      <div className="p-3">
        <p 
          onClick={onClick}
          className="font-medium text-gray-800 text-sm line-clamp-2 mb-2 leading-snug"
        >
          {blog.title}
        </p>
        
        {/* Author & Like */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5 overflow-hidden">
            <img 
              src={blog.icon} 
              alt={blog.name}
              className="w-5 h-5 rounded-full bg-gray-200" 
            />
            <span className="text-xs text-gray-500 truncate max-w-[60px]">
              {blog.name}
            </span>
          </div>
          
          <button 
            onClick={(e) => { 
              e.stopPropagation(); 
              onLike(); 
            }}
            className="flex items-center gap-1 text-xs text-gray-500 hover:text-red-500 transition-colors"
          >
            <Heart 
              size={14} 
              className={blog.isLike ? "fill-red-500 text-red-500" : ""} 
            />
            {blog.liked}
          </button>
        </div>
      </div>
    </div>
  );
};