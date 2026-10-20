// La URL se inyecta en tiempo de compilación con:
// BACKEND_URL=https://<app>.<region>.azurecontainerapps.io npm run build
// Para frontend y backend bajo el mismo host (Nginx en la VM):
// BACKEND_URL=same-origin npm run build
declare const __BACKEND_URL__: string;

export const environment = {
  production: true,
  apiUrl: __BACKEND_URL__,
};
