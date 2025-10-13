/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'valorant-red': '#ff4655',
        'valorant-dark': '#0f1923',
        'valorant-darker': '#0a0e13',
      },
    },
  },
  plugins: [],
}
