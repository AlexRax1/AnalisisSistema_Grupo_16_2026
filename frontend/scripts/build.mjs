import { spawnSync } from 'node:child_process';

const rawBackendUrl = process.env.BACKEND_URL?.trim() || 'http://localhost:8080';

// "same-origin" deja las llamadas como rutas relativas (/auth, /quejas, ...).
// Es el modo usado por Nginx en la VM, que hace de reverse proxy hacia el backend.
const backendUrl =
  rawBackendUrl === 'same-origin' || rawBackendUrl === '/'
    ? ''
    : rawBackendUrl.replace(/\/+$/, '');

const ngCommand = process.platform === 'win32' ? 'ng.cmd' : 'ng';

const result = spawnSync(
  ngCommand,
  [
    'build',
    '--configuration',
    'production',
    '--define',
    `__BACKEND_URL__=${JSON.stringify(backendUrl)}`,
  ],
  { stdio: 'inherit' },
);

if (result.error) {
  console.error(result.error);
  process.exit(1);
}

process.exit(result.status ?? 1);
