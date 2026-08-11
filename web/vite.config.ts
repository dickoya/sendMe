import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
    tsconfigPaths: true, // ✅ active la résolution native
  },
  build: {
    rollupOptions: {
      output: {
        // Découpe les bibliothèques dans des chunks séparés (cache + parallélisme)
        // au lieu d'un seul gros fichier JS. Forme "fonction" requise par Vite 8 (Rolldown).
        manualChunks(id) {
          if (!id.includes('node_modules')) return undefined;
          if (id.includes('recharts')) return 'charts';
          if (id.includes('@stripe')) return 'stripe';
          if (id.includes('i18next') || id.includes('react-i18next')) return 'i18n';
          if (id.includes('react')) return 'react';
          if (
            id.includes('axios') ||
            id.includes('date-fns') ||
            id.includes('lucide-react') ||
            id.includes('clsx') ||
            id.includes('class-variance-authority') ||
            id.includes('tailwind-merge')
          ) {
            return 'utils';
          }
          return 'vendor';
        },
      },
    },
  },
});
