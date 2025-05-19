import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  base: "./",

  build: {
    outDir: "dist",

    rollupOptions: {
      input: {
        input: "./index.html",
        main: "src/main.jsx",
      },
    }
  },

  plugins: [
    react(),
    tailwindcss(),
  ],
})
