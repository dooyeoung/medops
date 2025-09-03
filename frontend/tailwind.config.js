/** @type {import('tailwindcss').Config} */
module.exports = {

  darkMode: ["class"],
  content: [
    './pages/**/*.{ts,tsx}',
    './components/**/*.{ts,tsx}',
    './app/**/*.{ts,tsx}',
    './src/**/*.{ts,tsx}',
  ],
  prefix: "",
    theme: {
      container: {
        center: true,
        padding: "2rem",
        screens: {
          "2xl": "1400px",
        },
      },
      extend: {
        // Shadcn-UI가 사용하는 테마와 애니메이션 설정이 여기에 추가됩니다.
        // 이 부분은 `npx shadcn-ui init` 실행 시 자동으로 채워집니다.
      },
    },
    plugins: [require("tailwindcss-animate")
  ],
}