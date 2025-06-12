import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  base: "/",

  build: {
    outDir: "dist",
  },

  server: {
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/csrf': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    },
  },

  plugins: [
    react(),
    tailwindcss(),
  ],
})
