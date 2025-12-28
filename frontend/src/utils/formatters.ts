/**
 * Parse comma-separated image string to array
 * Backend returns: "img1.jpg,img2.jpg,img3.jpg"
 * Frontend needs: ["img1.jpg", "img2.jpg", "img3.jpg"]
 */
export const parseImages = (images: string): string[] => {
  if (!images) return [];
  return images.split(',').filter(Boolean);
};

/**
 * Format images array back to comma-separated string for API
 */
export const stringifyImages = (images: string[]): string => {
  return images.join(',');
};

/**
 * Format relative time (e.g., "2 hours ago", "3 days ago")
 */
export const formatRelativeTime = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (seconds < 60) return 'just now';
  if (seconds < 3600) return `${Math.floor(seconds / 60)} minutes ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)} hours ago`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)} days ago`;
  return date.toLocaleDateString();
};

/**
 * Format price from cents to dollars
 */
export const formatPrice = (cents: number): string => {
  return `$${(cents / 100).toFixed(2)}`;
};

/**
 * Format price as integer dollars (no cents)
 */
export const formatPriceInt = (cents: number): string => {
  return `$${Math.round(cents / 100)}`;
};

/**
 * Format score (1-50) to rating (0.0-5.0)
 */
export const formatRating = (score: number): string => {
  return (score / 10).toFixed(1);
};

/**
 * Format distance from meters to kilometers
 */
export const formatDistance = (meters: number): string => {
  const km = meters / 1000;
  return `${km.toFixed(1)} km`;
};