// src/pages/BlogFeed.tsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { BlogCard } from '@/components/blog/BlogCard';
import { blogApi } from '@/api/blog.api';
import type { Blog } from '@/api/types';

export const BlogFeed = () => {
  const navigate = useNavigate();
  
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [activeTab, setActiveTab] = useState<'hot' | 'following'>('hot');

  // Fetch initial blogs
  useEffect(() => {
    const fetchBlogs = async () => {
      try {
        setLoading(true);
        const response = await blogApi.getHotBlogs(1);
        
        if (response.success) {
          // ✅ Add defensive fallbacks
          const records = response.data?.records || response.data || [];
          setBlogs(records);
          setHasMore(response.data?.current < response.data?.pages);
          setPage(1);
        }
      } catch (error) {
        console.error('Failed to fetch blogs:', error);
        setBlogs([]); // ✅ Set empty array on error
      } finally {
        setLoading(false);
      }
    };

    fetchBlogs();
  }, [activeTab]);

  const handleBlogClick = (blog: Blog) => {
    navigate(`/blog/${blog.id}`);
  };

  const handleLikeToggle = async (blogId: number) => {
    try {
      await blogApi.likeBlog(blogId);
      
      // Optimistically update UI
      setBlogs(blogs.map(blog => {
        if (blog.id === blogId) {
          return {
            ...blog,
            isLike: !blog.isLike,
            liked: blog.isLike ? blog.liked - 1 : blog.liked + 1
          };
        }
        return blog;
      }));
    } catch (error) {
      console.error('Failed to toggle like:', error);
    }
  };

  const loadMore = async () => {
    if (!hasMore || loading) return;

    try {
      const nextPage = page + 1;
      const response = await blogApi.getHotBlogs(nextPage);

      if (response.success) {
        const records = response.data?.records || response.data || [];
        setBlogs([...blogs, ...records]);
        setHasMore(response.data?.current < response.data?.pages);
        setPage(nextPage);
      }
    } catch (error) {
      console.error('Failed to load more blogs:', error);
    }
  };

  return (
    <div className="pb-24 bg-gray-50 min-h-screen">
      {/* Header */}
      <div className="bg-white sticky top-0 z-10 px-4 py-3 border-b border-gray-100 flex justify-between items-center shadow-sm">
        <h2 className="font-serif font-bold text-xl">Discover</h2>
        <div className="flex gap-2 text-sm font-medium">
          <button
            onClick={() => setActiveTab('hot')}
            className={activeTab === 'hot' ? 'text-primary-500' : 'text-gray-400'}
          >
            Hot
          </button>
          <span className="text-gray-400">|</span>
          <button
            onClick={() => setActiveTab('following')}
            className={activeTab === 'following' ? 'text-primary-500' : 'text-gray-400'}
          >
            Following
          </button>
        </div>
      </div>

      {/* Loading State */}
      {loading && blogs.length === 0 ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin text-primary-500">⏳</div>
        </div>
      ) : blogs.length > 0 ? (
        <>
          {/* Masonry Grid */}
          <div className="p-2 columns-2 gap-2">
            {blogs.map(blog => (
              <div key={blog.id} className="break-inside-avoid mb-2">
                <BlogCard 
                  blog={blog}
                  onClick={() => handleBlogClick(blog)}
                  onLike={() => handleLikeToggle(blog.id)}
                />
              </div>
            ))}
          </div>

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
          <p>No posts yet</p>
        </div>
      )}
    </div>
  );
};