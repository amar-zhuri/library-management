import colors from 'tailwindcss/colors'

/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Manrope"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      colors: {
        primary: colors.indigo,
        muted: colors.slate,
      },
      boxShadow: {
        card: '0 10px 30px rgba(0,0,0,0.05)',
      },
    },
  },
  plugins: [],
}
