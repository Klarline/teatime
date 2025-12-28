/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f5f7f0',
          100: '#e8edd8',
          200: '#d4dcb8',
          300: '#b8c68d',
          400: '#96a85e',
          500: '#425512',  // Main color - Clover Green
          600: '#3a4a10',
          700: '#2f3c0d',
          800: '#242d0a',
          900: '#1a2008',
        },
        secondary: {
          50: '#fdf8f6',
          100: '#f2e8e5',
          200: '#eaddd7',
          300: '#e0cec7',
          400: '#d2bab0',
          500: '#98592c',  // Warm tea brown
          600: '#7c4a24',
          700: '#603a1d',
          800: '#442a15',
          900: '#281a0d',
        },
        accent: {
          50: '#f7fee7',
          100: '#ecfccb',
          200: '#d9f99d',
          300: '#bef264',
          400: '#a3e635',
          500: '#77af64',  // Matcha green
          600: '#65a30d',
          700: '#4d7c0f',
          800: '#3f6212',
          900: '#365314',
        },
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        serif: ['Playfair Display', 'serif'],
      },
      spacing: {
        'safe-top': 'env(safe-area-inset-top)',
        'safe-bottom': 'env(safe-area-inset-bottom)',
        'safe-left': 'env(safe-area-inset-left)',
        'safe-right': 'env(safe-area-inset-right)',
      },
      aspectRatio: {
        '4/5': '4 / 5',
      },
    },
  },
  plugins: [
    // Custom plugin for utilities
    function({ addUtilities }) {
      const newUtilities = {
        '.no-scrollbar': {
          '-ms-overflow-style': 'none',
          'scrollbar-width': 'none',
          '&::-webkit-scrollbar': {
            display: 'none',
          },
        },
        '.pb-safe': {
          'padding-bottom': 'env(safe-area-inset-bottom)',
        },
        '.pt-safe': {
          'padding-top': 'env(safe-area-inset-top)',
        },
        '.pl-safe': {
          'padding-left': 'env(safe-area-inset-left)',
        },
        '.pr-safe': {
          'padding-right': 'env(safe-area-inset-right)',
        },
      }
      addUtilities(newUtilities)
    },
  ],
}