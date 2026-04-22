import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    allowedHosts: ['villagets.lesageserveur.com'],
    watch: {
      usePolling: true,
    },
    hmr: {
      clientPort: 420,
    },
  },
})
