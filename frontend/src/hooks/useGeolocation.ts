import { useState, useCallback } from 'react';

interface Coordinates {
  x: number; // longitude
  y: number; // latitude
}

interface GeolocationState {
  coords: Coordinates | null;
  error: string | null;
  loading: boolean;
  isSupported: boolean;
}

interface UseGeolocationReturn extends GeolocationState {
  getLocation: () => void;
  clearError: () => void;
}

/**
 * Hook to get user's current geolocation
 * Returns coordinates in backend format (x: longitude, y: latitude)
 */
export const useGeolocation = (): UseGeolocationReturn => {
  const [coords, setCoords] = useState<Coordinates | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const isSupported = 'geolocation' in navigator;

  const getLocation = useCallback(() => {
    if (!isSupported) {
      setError('Geolocation is not supported by your browser');
      return;
    }

    setLoading(true);
    setError(null);

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCoords({
          x: position.coords.longitude,
          y: position.coords.latitude,
        });
        setError(null);
        setLoading(false);
      },
      (err) => {
        let errorMessage = 'Failed to get location';
        
        switch (err.code) {
          case err.PERMISSION_DENIED:
            errorMessage = 'Location permission denied';
            break;
          case err.POSITION_UNAVAILABLE:
            errorMessage = 'Location information unavailable';
            break;
          case err.TIMEOUT:
            errorMessage = 'Location request timed out';
            break;
        }
        
        setError(errorMessage);
        setLoading(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0,
      }
    );
  }, [isSupported]);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    coords,
    error,
    loading,
    isSupported,
    getLocation,
    clearError,
  };
};