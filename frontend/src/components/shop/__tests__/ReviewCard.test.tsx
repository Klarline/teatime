import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { ReviewCard } from '../ReviewCard';
import type { Review } from '@/types';

// Mock date-fns
jest.mock('date-fns', () => ({
  formatDistanceToNow: jest.fn(() => '2 days ago')
}));

const mockReview: Review = {
  id: 1,
  shopId: 1,
  userId: 10,
  rating: 4,
  content: 'Great tea selection and cozy atmosphere!',
  createTime: '2024-01-13T10:00:00',
  updateTime: '2024-01-13T10:00:00',
  nickName: 'John Doe',
  icon: 'user-icon.jpg'
};

describe('ReviewCard', () => {
  it('renders review content', () => {
    render(<ReviewCard review={mockReview} />);
    
    expect(screen.getByText('Great tea selection and cozy atmosphere!')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('2 days ago')).toBeInTheDocument();
  });

  it('displays correct number of filled stars', () => {
    const { container } = render(<ReviewCard review={mockReview} />);
    
    const filledStars = container.querySelectorAll('.fill-yellow-400');
    expect(filledStars.length).toBe(4);
  });

  it('displays 5 stars for 5-star rating', () => {
    const fiveStarReview = { ...mockReview, rating: 5 };
    const { container } = render(<ReviewCard review={fiveStarReview} />);
    
    const filledStars = container.querySelectorAll('.fill-yellow-400');
    expect(filledStars.length).toBe(5);
  });

  it('shows delete button for owner', () => {
    render(<ReviewCard review={mockReview} currentUserId={10} onDelete={jest.fn()} />);
    
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  it('hides delete button for non-owner', () => {
    render(<ReviewCard review={mockReview} currentUserId={99} onDelete={jest.fn()} />);
    
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('calls onDelete when delete button clicked', async () => {
    const handleDelete = jest.fn();
    render(<ReviewCard review={mockReview} currentUserId={10} onDelete={handleDelete} />);
    
    const deleteButton = screen.getByRole('button');
    await userEvent.click(deleteButton);
    
    expect(handleDelete).toHaveBeenCalledWith(1);
  });

  it('disables delete button when deleting', () => {
    render(<ReviewCard review={mockReview} currentUserId={10} onDelete={jest.fn()} isDeleting={true} />);
    
    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('displays default avatar when no icon', () => {
    const reviewWithoutIcon = { ...mockReview, icon: undefined };
    render(<ReviewCard review={reviewWithoutIcon} />);
    
    const avatar = screen.getByAltText('John Doe');
    expect(avatar).toHaveAttribute('src', '/default-avatar.png');
  });

  it('displays Anonymous when no nickname', () => {
    const reviewWithoutName = { ...mockReview, nickName: undefined };
    render(<ReviewCard review={reviewWithoutName} />);
    
    expect(screen.getByText('Anonymous')).toBeInTheDocument();
  });
});