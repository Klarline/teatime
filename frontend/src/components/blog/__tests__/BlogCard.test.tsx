import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { BlogCard } from '../BlogCard';
import type { Blog } from '@/types';

const mockBlog: Blog = {
  id: 1,
  shopId: 1,
  userId: 10,
  title: 'Amazing Matcha Experience',
  images: 'blog-image.jpg',
  content: 'This is a great blog post about matcha',
  liked: 5,
  comments: 3,
  createTime: '2024-01-15T10:00:00',
  name: 'John Doe',
  icon: 'user-icon.jpg',
  isLike: false
};

describe('BlogCard', () => {
  it('renders blog information correctly', () => {
    const handleClick = jest.fn();
    const handleLike = jest.fn();
    render(<BlogCard blog={mockBlog} onClick={handleClick} onLike={handleLike} />);
    
    expect(screen.getByText('Amazing Matcha Experience')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it('calls onClick when card is clicked', async () => {
    const handleClick = jest.fn();
    const handleLike = jest.fn();
    render(<BlogCard blog={mockBlog} onClick={handleClick} onLike={handleLike} />);
    
    const title = screen.getByText('Amazing Matcha Experience');
    await userEvent.click(title);
    
    expect(handleClick).toHaveBeenCalledTimes(1);
    expect(handleLike).not.toHaveBeenCalled();
  });

  it('calls onLike when like button is clicked', async () => {
    const handleClick = jest.fn();
    const handleLike = jest.fn();
    render(<BlogCard blog={mockBlog} onClick={handleClick} onLike={handleLike} />);
    
    const likeButton = screen.getByRole('button');
    await userEvent.click(likeButton);
    
    expect(handleLike).toHaveBeenCalledTimes(1);
    expect(handleClick).not.toHaveBeenCalled();
  });

  it('displays liked state correctly when blog is liked', () => {
    const handleClick = jest.fn();
    const handleLike = jest.fn();
    const likedBlog = { ...mockBlog, isLike: true };
    
    const { container } = render(
      <BlogCard blog={likedBlog} onClick={handleClick} onLike={handleLike} />
    );
    
    // Heart icon should have fill-red-500 class when liked
    const heartIcon = container.querySelector('.fill-red-500');
    expect(heartIcon).toBeInTheDocument();
  });

  it('displays unliked state correctly when blog is not liked', () => {
    const handleClick = jest.fn();
    const handleLike = jest.fn();
    const { container } = render(
      <BlogCard blog={mockBlog} onClick={handleClick} onLike={handleLike} />
    );
    
    // Heart icon should not have fill-red-500 class when not liked
    const heartIcon = container.querySelector('.fill-red-500');
    expect(heartIcon).not.toBeInTheDocument();
  });

  it('displays author icon and name', () => {
    const handleClick = jest.fn();
    const handleLike = jest.fn();
    render(<BlogCard blog={mockBlog} onClick={handleClick} onLike={handleLike} />);
    
    const authorImage = screen.getByAltText('John Doe');
    expect(authorImage).toBeInTheDocument();
    expect(authorImage).toHaveAttribute('src', 'user-icon.jpg');
  });

  it('displays blog image', () => {
    const handleClick = jest.fn();
    const handleLike = jest.fn();
    render(<BlogCard blog={mockBlog} onClick={handleClick} onLike={handleLike} />);
    
    const blogImage = screen.getByAltText('Amazing Matcha Experience');
    expect(blogImage).toBeInTheDocument();
    expect(blogImage).toHaveAttribute('src', 'blog-image.jpg');
  });

  it('displays like count', () => {
    const handleClick = jest.fn();
    const handleLike = jest.fn();
    render(<BlogCard blog={mockBlog} onClick={handleClick} onLike={handleLike} />);
    
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it('handles blogs with zero likes', () => {
    const handleClick = jest.fn();
    const handleLike = jest.fn();
    const blogWithZeroLikes = { ...mockBlog, liked: 0 };
    
    render(<BlogCard blog={blogWithZeroLikes} onClick={handleClick} onLike={handleLike} />);
    
    expect(screen.getByText('0')).toBeInTheDocument();
  });
});