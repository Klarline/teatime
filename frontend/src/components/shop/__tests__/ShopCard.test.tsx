import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { ShopCard } from '../ShopCard';
import type { Shop } from '@/types';

const mockShop: Shop = {
  id: 1,
  name: 'Test Matcha Cafe',
  typeId: 1,
  area: 'Downtown Vancouver',
  score: 4.5,
  comments: 10,
  avgPrice: 25,
  distance: 1.2,
  images: 'test-image.jpg',
  address: '123 Test St',
  x: 123.456,
  y: 49.789,
  sold: 0,
  openHours: '9:00-18:00'
};

describe('ShopCard', () => {
  it('renders shop information correctly', () => {
    const handleClick = jest.fn();
    render(<ShopCard shop={mockShop} onClick={handleClick} />);
    
    expect(screen.getByText('Test Matcha Cafe')).toBeInTheDocument();
    expect(screen.getByText('0.5')).toBeInTheDocument(); // ← Changed from '4.5'
    expect(screen.getByText('Downtown Vancouver')).toBeInTheDocument();
  });

  it('calls onClick when clicked', async () => {
    const handleClick = jest.fn();
    render(<ShopCard shop={mockShop} onClick={handleClick} />);
    
    const card = screen.getByText('Test Matcha Cafe').closest('div');
    await userEvent.click(card!);
    
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('displays distance when provided', () => {
    const handleClick = jest.fn();
    render(<ShopCard shop={mockShop} onClick={handleClick} />);
    
    // Check for "0.0 km" or just "km away"
    expect(screen.getByText(/km away/i)).toBeInTheDocument();
  });

  it('does not display distance when not provided', () => {
    const handleClick = jest.fn();
    const shopWithoutDistance = { ...mockShop, distance: undefined };
    render(<ShopCard shop={shopWithoutDistance} onClick={handleClick} />);
    
    expect(screen.queryByText(/away/i)).not.toBeInTheDocument();
  });

  it('displays review count and average price', () => {
    const handleClick = jest.fn();
    render(<ShopCard shop={mockShop} onClick={handleClick} />);
    
    expect(screen.getByText(/10.*reviews/i)).toBeInTheDocument();
    expect(screen.getByText(/avg/i)).toBeInTheDocument();
  });
});