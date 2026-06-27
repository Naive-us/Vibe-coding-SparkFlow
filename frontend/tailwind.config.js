/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        oat: {
          50: '#FDFBF7',
          100: '#F5F0E8',
          200: '#E8DFD0',
          300: '#D4C4A8',
          400: '#BFA882',
          500: '#A68B5B',
        },
        warm: {
          wood: '#8B7355',
          cream: '#FAF6F0',
        },
      },
      fontFamily: {
        sans: ['"Noto Sans SC"', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
