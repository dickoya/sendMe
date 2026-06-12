import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      // Permet d'importer avec "@/components/..." au lieu de "../../components/..."
      '@': path.resolve(__dirname, './src'),
    },
  },
});