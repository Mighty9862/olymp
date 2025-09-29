import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [tailwindcss(), react()],
  preview: {
    port: 3000,
    strictPort: true,
  },
  server: {
    port: 3000,
    strictPort: true,
    host: true,
    origin: "http://0.0.0.0:80",
    // Прокси для API в development режиме
    proxy: {
      '/api': {
        target: 'http://localhost:8300',
        changeOrigin: true,
        secure: false,
      },
      '/uploads': {
        target: 'http://localhost:8300',
        changeOrigin: true,
        secure: false,
      }
    }
  }
});