import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { CategoryGrid } from '../CategoryGrid';
import type { ShopType } from '@/types';

const mockCategories: ShopType[] = [
  { id: 1, name: 'Bubble Tea', icon: '🧋', sort: 1 },
  { id: 2, name: 'Matcha Cafes', icon: '🍵', sort: 2 },
  { id: 3, name: 'Traditional Tea', icon: '🫖', sort: 3 }
];

describe('CategoryGrid', () => {
  it('renders all categories', () => {
    const handleClick = jest.fn();
    render(<CategoryGrid categories={mockCategories} onCategoryClick={handleClick} />);
    
    expect(screen.getByText('Bubble Tea')).toBeInTheDocument();
    expect(screen.getByText('Matcha Cafes')).toBeInTheDocument();
    expect(screen.getByText('Traditional Tea')).toBeInTheDocument();
  });

  it('displays category icons', () => {
    const handleClick = jest.fn();
    render(<CategoryGrid categories={mockCategories} onCategoryClick={handleClick} />);
    
    expect(screen.getByText('🧋')).toBeInTheDocument();
    expect(screen.getByText('🍵')).toBeInTheDocument();
    expect(screen.getByText('🫖')).toBeInTheDocument();
  });

  it('calls onCategoryClick with correct id', async () => {
    const handleClick = jest.fn();
    render(<CategoryGrid categories={mockCategories} onCategoryClick={handleClick} />);
    
    await userEvent.click(screen.getByText('Bubble Tea'));
    expect(handleClick).toHaveBeenCalledWith(1);
    
    await userEvent.click(screen.getByText('Matcha Cafes'));
    expect(handleClick).toHaveBeenCalledWith(2);
  });

  it('renders empty grid when no categories', () => {
    const handleClick = jest.fn();
    const { container } = render(<CategoryGrid categories={[]} onCategoryClick={handleClick} />);
    
    expect(container.querySelector('.grid')).toBeInTheDocument();
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });
});