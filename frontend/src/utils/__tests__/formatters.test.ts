import {
  parseImages,
  stringifyImages,
  formatRelativeTime,
  formatPrice,
  formatPriceInt,
  formatRating,
  formatDistance
} from '../formatters';

describe('formatters', () => {
  describe('parseImages', () => {
    it('parses comma-separated string to array', () => {
      const result = parseImages('img1.jpg,img2.jpg,img3.jpg');
      expect(result).toEqual(['img1.jpg', 'img2.jpg', 'img3.jpg']);
    });

    it('returns empty array for empty string', () => {
      const result = parseImages('');
      expect(result).toEqual([]);
    });

    it('returns empty array for null/undefined', () => {
      const result = parseImages(null as any);
      expect(result).toEqual([]);
    });

    it('filters out empty strings', () => {
      const result = parseImages('img1.jpg,,img2.jpg');
      expect(result).toEqual(['img1.jpg', 'img2.jpg']);
    });

    it('handles single image', () => {
      const result = parseImages('single.jpg');
      expect(result).toEqual(['single.jpg']);
    });
  });

  describe('stringifyImages', () => {
    it('joins array to comma-separated string', () => {
      const result = stringifyImages(['img1.jpg', 'img2.jpg', 'img3.jpg']);
      expect(result).toBe('img1.jpg,img2.jpg,img3.jpg');
    });

    it('returns empty string for empty array', () => {
      const result = stringifyImages([]);
      expect(result).toBe('');
    });

    it('handles single image', () => {
      const result = stringifyImages(['single.jpg']);
      expect(result).toBe('single.jpg');
    });
  });

  describe('formatRelativeTime', () => {
    beforeEach(() => {
      // Mock current time to 2024-01-15 12:00:00
      jest.useFakeTimers();
      jest.setSystemTime(new Date('2024-01-15T12:00:00Z'));
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('returns "just now" for times within last minute', () => {
      const result = formatRelativeTime('2024-01-15T11:59:30Z');
      expect(result).toBe('just now');
    });

    it('returns minutes ago for times within last hour', () => {
      const result = formatRelativeTime('2024-01-15T11:30:00Z');
      expect(result).toBe('30 minutes ago');
    });

    it('returns hours ago for times within last day', () => {
      const result = formatRelativeTime('2024-01-15T08:00:00Z');
      expect(result).toBe('4 hours ago');
    });

    it('returns days ago for times within last week', () => {
      const result = formatRelativeTime('2024-01-13T12:00:00Z');
      expect(result).toBe('2 days ago');
    });

    it('returns formatted date for times older than a week', () => {
      const result = formatRelativeTime('2024-01-01T12:00:00Z');
      expect(result).toMatch(/1\/1\/2024|2024-01-01/); // Handles locale differences
    });
  });

  describe('formatPrice', () => {
    it('formats cents to dollars with 2 decimals', () => {
      expect(formatPrice(2500)).toBe('$25.00');
    });

    it('handles zero price', () => {
      expect(formatPrice(0)).toBe('$0.00');
    });

    it('handles prices with cents', () => {
      expect(formatPrice(1299)).toBe('$12.99');
    });

    it('handles single digit cents', () => {
      expect(formatPrice(1005)).toBe('$10.05');
    });

    it('handles large amounts', () => {
      expect(formatPrice(999999)).toBe('$9999.99');
    });
  });

  describe('formatPriceInt', () => {
    it('formats cents to rounded dollars', () => {
      expect(formatPriceInt(2500)).toBe('$25');
    });

    it('rounds up from 50 cents', () => {
      expect(formatPriceInt(2550)).toBe('$26');
    });

    it('rounds down below 50 cents', () => {
      expect(formatPriceInt(2549)).toBe('$25');
    });

    it('handles zero price', () => {
      expect(formatPriceInt(0)).toBe('$0');
    });
  });

  describe('formatRating', () => {
    it('formats score (1-50) to rating (0.0-5.0)', () => {
      expect(formatRating(45)).toBe('4.5');
    });

    it('handles perfect score', () => {
      expect(formatRating(50)).toBe('5.0');
    });

    it('handles zero score', () => {
      expect(formatRating(0)).toBe('0.0');
    });

    it('handles decimal precision', () => {
      expect(formatRating(37)).toBe('3.7');
    });

    it('formats with one decimal place', () => {
      expect(formatRating(42)).toBe('4.2');
    });
  });

  describe('formatDistance', () => {
    it('formats meters to kilometers with 1 decimal', () => {
      expect(formatDistance(1200)).toBe('1.2 km');
    });

    it('handles zero distance', () => {
      expect(formatDistance(0)).toBe('0.0 km');
    });

    it('handles small distances', () => {
      expect(formatDistance(500)).toBe('0.5 km');
    });

    it('handles large distances', () => {
      expect(formatDistance(15000)).toBe('15.0 km');
    });

    it('rounds to 1 decimal place', () => {
      expect(formatDistance(1234)).toBe('1.2 km');
    });
  });
});