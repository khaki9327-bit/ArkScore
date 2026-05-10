import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}"
  ],
  theme: {
    extend: {
      colors: {
        ink: "#050711",
        panel: "#0b1020",
        line: "rgba(148, 163, 184, 0.18)",
        solana: {
          cyan: "#14f1d9",
          violet: "#9945ff",
          blue: "#2d7ff9"
        }
      },
      boxShadow: {
        glow: "0 0 48px rgba(20, 241, 217, 0.16)"
      }
    }
  },
  plugins: []
};

export default config;
