import { Routes } from '@angular/router';
import { MisQuejasComponent } from './modules/portal-ciudadano/pages/mis-quejas/mis-quejas';
import { CrearQuejaComponent } from './modules/portal-ciudadano/pages/crear-queja/crear-queja';
import { LoginComponent } from './modules/auth/pages/login/login';
import { RegistroComponent } from './modules/auth/pages/registro/registro';
import { BandejaGestionComponent } from './modules/gestion-municipal/pages/bandeja-gestion/bandeja-gestion';
import { InspeccionCuadrillaComponent } from './modules/operaciones-campo/pages/inspeccion-cuadrilla/inspeccion-cuadrilla';

export const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/registro', component: RegistroComponent },
  { path: 'portal-ciudadano', component: MisQuejasComponent },
  { path: 'portal-ciudadano/crear', component: CrearQuejaComponent },
  { path: 'gestion-municipal', component: BandejaGestionComponent },
  { path: 'operaciones-campo', component: InspeccionCuadrillaComponent },
  { path: '**', redirectTo: 'auth/login' }
];
