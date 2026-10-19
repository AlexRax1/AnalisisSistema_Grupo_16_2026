import { spawnSync } from 'node:child_process';

const backendUrl = process.env.BACKEND_URL?.trim().replace(/\/+$/, '');

if (!backendUrl) {
  console.error('');
  console.error('ERROR: falta la variable de entorno BACKEND_URL.');
  console.error('Ejemplo:');
  console.error('  BACKEND_URL=https://mi-backend.azurecontainerapps.io npm run build');
  console.error('');
  process.exit(1);
}

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
