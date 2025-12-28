// src/pages/BlogDetail.tsx
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  ChevronLeft, MoreHorizontal, Heart, MessageCircle, 
  Share2, Edit3 
} from 'lucide-react';
import { blogApi } from '@/api/blog.api';
import { parseImages, formatRelativeTime } from '@/utils/formatters';
import type { Blog, User } from '@/api/types';

export const BlogDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [blog, setBlog] = useState<Blog | null>(null);
  const [likedBy, setLikedBy] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  useEffect(() => {
    const fetchBlogData = async () => {
      if (!id) return;

      try {
        setLoading(true);

        // Fetch blog details
        const blogResponse = await blogApi.getBlogById(parseInt(id));
        if (blogResponse.success) {
          setBlog(blogResponse.data);
        }

        // Fetch users who liked this blog
        const likesResponse = await blogApi.getBlogLikes(parseInt(id));
        if (likesResponse.success) {
          // ✅ Add defensive fallback
          setLikedBy(likesResponse.data || []);
        }
      } catch (error) {
        console.error('Failed to fetch blog data:', error);
        setLikedBy([]); // ✅ Set empty array on error
      } finally {
        setLoading(false);
      }
    };

    fetchBlogData();
  }, [id]);

  const handleBack = () => {
    navigate(-1);
  };

  const handleLike = async () => {
    if (!blog) return;

    try {
      await blogApi.likeBlog(blog.id);
      
      // Optimistically update UI
      setBlog({
        ...blog,
        isLike: !blog.isLike,
        liked: blog.isLike ? blog.liked - 1 : blog.liked + 1
      });

      // Refresh liked by list
      const likesResponse = await blogApi.getBlogLikes(blog.id);
      if (likesResponse.success) {
        // ✅ Add defensive fallback
        setLikedBy(likesResponse.data || []);
      }
    } catch (error) {
      console.error('Failed to toggle like:', error);
    }
  };

  const handleShare = () => {
    if (navigator.share && blog) {
      navigator.share({
        title: blog.title,
        text: blog.content.substring(0, 100) + '...',
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

  if (!blog) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-8">
        <p className="text-gray-500 mb-4">Blog post not found</p>
        <button 
          onClick={handleBack}
          className="text-primary-500 underline"
        >
          Go Back
        </button>
      </div>
    );
  }

  const images = parseImages(blog.images);

  return (
    <div className="bg-white min-h-screen pb-24">
      {/* Header */}
      <div className="sticky top-0 bg-white z-50 px-4 py-3 flex justify-between items-center border-b border-gray-100">
        <button 
          onClick={handleBack}
          className="p-1 rounded-full hover:bg-gray-100"
        >
          <ChevronLeft size={24} className="text-gray-700" />
        </button>
        <button className="p-1 rounded-full hover:bg-gray-100">
          <MoreHorizontal size={24} className="text-gray-700" />
        </button>
      </div>

      {/* Author Info */}
      <div className="px-4 py-3 flex justify-between items-center">
        <div className="flex items-center gap-3">
          <img 
            src={blog.icon} 
            alt={blog.name}
            className="w-10 h-10 rounded-full border border-gray-200" 
          />
          <div>
            <h3 className="font-bold text-sm text-gray-900">{blog.name}</h3>
            <p className="text-xs text-gray-400">{formatRelativeTime(blog.createTime)}</p>
          </div>
        </div>
        <button className="px-4 py-1.5 border border-primary-500 text-primary-500 text-xs font-bold rounded-full hover:bg-green-50 transition-colors">
          Follow
        </button>
      </div>

      {/* Images */}
      <div className="w-full aspect-[4/5] bg-gray-100 relative">
        <img 
          src={images[currentImageIndex]} 
          alt={blog.title}
          className="w-full h-full object-cover" 
        />
        
        {/* Pagination Dots */}
        {images.length > 1 && (
          <div className="absolute bottom-4 left-0 right-0 flex justify-center gap-2">
            {images.map((_, index) => (
              <button
                key={index}
                onClick={() => setCurrentImageIndex(index)}
                className={`w-2 h-2 rounded-full shadow transition-colors ${
                  index === currentImageIndex ? 'bg-white' : 'bg-white/50'
                }`}
              />
            ))}
          </div>
        )}
      </div>

      {/* Content */}
      <div className="px-4 py-4">
        <h1 className="text-lg font-bold text-gray-900 mb-2">{blog.title}</h1>
        <p className="text-gray-600 text-sm leading-relaxed whitespace-pre-wrap">
          {blog.content}
        </p>
        
        {/* Tags (mock for now) */}
        <div className="mt-4 flex gap-2 flex-wrap">
          <span className="text-primary-500 text-xs font-medium">#teatime</span>
          <span className="text-primary-500 text-xs font-medium">#tea</span>
        </div>
      </div>

      {/* Interaction Bar */}
      <div className="px-4 py-3 border-t border-b border-gray-100 flex justify-between items-center">
        <div className="flex gap-4">
          <div className="flex flex-col items-center">
            <button onClick={handleLike} className="mb-1">
              <Heart 
                size={24} 
                className={blog.isLike ? "fill-red-500 text-red-500 transition-colors" : "text-gray-700"}
              />
            </button>
            <span className="text-xs text-gray-500">{blog.liked}</span>
          </div>
          <div className="flex flex-col items-center">
            <button className="mb-1">
              <MessageCircle size={24} className="text-gray-700" />
            </button>
            <span className="text-xs text-gray-500">{blog.comments}</span>
          </div>
          <div className="flex flex-col items-center">
            <button onClick={handleShare} className="mb-1">
              <Share2 size={24} className="text-gray-700" />
            </button>
            <span className="text-xs text-gray-500">Share</span>
          </div>
        </div>
      </div>

      {/* Liked By */}
      {likedBy.length > 0 && (
        <div className="px-4 py-4 flex items-center justify-between">
          <div className="flex items-center -space-x-2">
            {likedBy.slice(0, 3).map((user) => (
              <img 
                key={user.id}
                src={user.icon} 
                alt={user.nickName}
                className="w-6 h-6 rounded-full border-2 border-white bg-gray-200" 
              />
            ))}
            {likedBy.length > 3 && (
              <div className="w-6 h-6 rounded-full border-2 border-white bg-gray-100 flex items-center justify-center text-[10px] text-gray-500 font-bold">
                +{likedBy.length - 3}
              </div>
            )}
          </div>
          <span className="text-xs text-gray-400">
            Liked by <b>{likedBy[0]?.nickName}</b>
            {likedBy.length > 1 && ` and ${likedBy.length - 1} others`}
          </span>
        </div>
      )}

      {/* Comment Input */}
      <div className="px-4 py-3 border-t border-gray-100 flex gap-3 items-center sticky bottom-0 bg-white">
        <div className="flex-1 bg-gray-100 rounded-full px-4 py-2 flex items-center gap-2">
          <Edit3 size={14} className="text-gray-400" />
          <input 
            placeholder="Add a comment..." 
            className="bg-transparent text-sm w-full focus:outline-none"
            disabled
          />
        </div>
        <button className="text-primary-500 font-medium text-sm">Post</button>
      </div>
    </div>
  );
};