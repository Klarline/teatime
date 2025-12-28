import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface UserState {
  // State
  checkinStreak: number;
  lastCheckinDate: string | null;
  
  // Actions
  setCheckinStreak: (streak: number) => void;
  incrementStreak: () => void;
  resetStreak: () => void;
  updateLastCheckin: (date: string) => void;
}

export const useUserStore = create<UserState>()(
  persist(
    (set) => ({
      // Initial state
      checkinStreak: 0,
      lastCheckinDate: null,

      // Set streak
      setCheckinStreak: (streak: number) => {
        set({ checkinStreak: streak });
      },

      // Increment streak
      incrementStreak: () => {
        set((state) => ({ 
          checkinStreak: state.checkinStreak + 1 
        }));
      },

      // Reset streak
      resetStreak: () => {
        set({ checkinStreak: 0 });
      },

      // Update last checkin date
      updateLastCheckin: (date: string) => {
        set({ lastCheckinDate: date });
      },
    }),
    {
      name: 'user-storage', // localStorage key
    }
  )
);