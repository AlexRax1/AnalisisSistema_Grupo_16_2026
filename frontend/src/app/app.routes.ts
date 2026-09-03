import { Routes } from '@angular/router';

import { PortalComponent } from './modules/portal/portal';
import { AuthLayoutComponent } from './shared/layouts/auth-layout/auth-layout';
import { DashboardLayoutComponent } from './shared/layouts/dashboard-layout/dashboard-layout';
import { LoginComponent } from './modules/auth/pages/login/login';
import { RegistroComponent } from './modules/auth/pages/registro/registro';
import { MisQuejasComponent } from './modules/portal-ciudadano/pages/mis-quejas/mis-quejas';
import { CrearQuejaComponent } from './modules/portal-ciudadano/pages/crear-queja/crear-queja';
import { BandejaGestionComponent } from './modules/gestion-municipal/pages/bandeja-gestion/bandeja-gestion';
import { InspeccionCuadrillaComponent } from './modules/operaciones-campo/pages/inspeccion-cuadrilla/inspeccion-cuadrilla';
import { AdminUsuariosComponent } from './modules/admin/pages/admin-usuarios/admin-usuarios';
import { AdminReportesComponent } from './modules/admin/pages/admin-reportes/admin-reportes';

export const routes: Routes = [
  // Portal público de bienvenida
  { path: '', component: PortalComponent },

  // Auth (layout sin navbar)
  {
    path: 'auth',
    component: AuthLayoutComponent,
    children: [
      { path: 'login', component: LoginComponent },
      { path: 'registro', component: RegistroComponent },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  // Dashboard: Ciudadano
  {
    path: 'ciudadano',
    component: DashboardLayoutComponent,
    children: [
      { path: 'mis-quejas', component: MisQuejasComponent },
      { path: 'nueva-queja', component: CrearQuejaComponent },
      { path: '', redirectTo: 'mis-quejas', pathMatch: 'full' }
    ]
  },

  // Dashboard: Funcionario Municipal
  {
    path: 'funcionario',
    component: DashboardLayoutComponent,
    children: [
      { path: 'bandeja', component: BandejaGestionComponent },
      { path: '', redirectTo: 'bandeja', pathMatch: 'full' }
    ]
  },

  // Dashboard: Inspector de Campo
  {
    path: 'inspector',
    component: DashboardLayoutComponent,
    children: [
      { path: 'inspecciones', component: InspeccionCuadrillaComponent },
      { path: '', redirectTo: 'inspecciones', pathMatch: 'full' }
    ]
  },

  // Dashboard: Especialista Técnico
  {
    path: 'especialista',
    component: DashboardLayoutComponent,
    children: [
      { path: 'ordenes', component: InspeccionCuadrillaComponent },
      { path: '', redirectTo: 'ordenes', pathMatch: 'full' }
    ]
  },

  // Dashboard: Administrador
  {
    path: 'admin',
    component: DashboardLayoutComponent,
    children: [
      { path: 'usuarios', component: AdminUsuariosComponent },
      { path: 'reportes', component: AdminReportesComponent },
      { path: '', redirectTo: 'usuarios', pathMatch: 'full' }
    ]
  },

  // Fallback
  { path: '**', redirectTo: '' }
];
