/** @type {import('tailwindcss').Config} */
module.exports = {
    content: ["./src/**/*.{html,js}"],

darkMode: 'class', // Hỗ trợ Dark Mode nếu Hiếu muốn dùng
  theme: {
    extend: {
      colors: {
        // Định nghĩa màu Primary xanh y tế
        "primary": "#007bff",
        "background-light": "#f5f7f8",
        "background-dark": "#0f1923",
      },
      fontFamily: {
        "display": ["Inter", "sans-serif"]
      }
    },
  },

plugins: [
    require('@tailwindcss/forms'),
  ],
}

