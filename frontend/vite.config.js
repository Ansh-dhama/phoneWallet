import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

function createProxy(target) {
  return {
    '/api': {
      target,
      changeOrigin: true,
      secure: false,
    },
  };
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const backendTarget = env.VITE_BACKEND_TARGET || 'http://localhost:8090';

  return {
    plugins: [react()],
    server: {
      port: 5173,
      host: true,
      proxy: createProxy(backendTarget),
    },
    preview: {
      port: 4173,
      host: true,
      proxy: createProxy(backendTarget),
    },
  };
});
