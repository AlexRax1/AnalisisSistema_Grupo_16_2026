import { Routes } from '@angular/router';

import { PortalComponent } from './modules/portal/portal';
import { AuthLayoutComponent } from './shared/layouts/auth-layout/auth-layout';
import { DashboardLayoutComponent } from './shared/layouts/dashboard-layout/dashboard-layout';
import { LoginComponent } from './modules/auth/pages/login/login';
import { RegistroComponent } from './modules/auth/pages/registro/registro';
import { MisQuejasComponent } from './modules/portal-ciudadano/pages/mis-quejas/mis-quejas';
import { CrearQuejaComponent } from './modules/portal-ciudadano/pages/crear-queja/crear-queja';
import { BandejaGestionComponent } from './modules/gestion-municipal/pages/bandeja-gestion/bandeja-gestion';
import { DetalleQuejaGestionComponent } from './modules/gestion-municipal/pages/detalle-queja-gestion/detalle-queja-gestion';
import { BandejaInspectorComponent } from './modules/operaciones-campo/pages/bandeja-inspector/bandeja-inspector';
import { InicioInspectorComponent } from './modules/operaciones-campo/pages/inicio-inspector/inicio-inspector';
import { DetalleQuejaInspectorComponent } from './modules/operaciones-campo/pages/detalle-queja-inspector/detalle-queja-inspector';
import { BandejaEspecialistaComponent } from './modules/operaciones-campo/pages/bandeja-especialista/bandeja-especialista';
import { InicioEspecialistaComponent } from './modules/operaciones-campo/pages/inicio-especialista/inicio-especialista';
import { DetalleQuejaEspecialistaComponent } from './modules/operaciones-campo/pages/detalle-queja-especialista/detalle-queja-especialista';
import { AdminUsuariosComponent } from './modules/admin/pages/admin-usuarios/admin-usuarios';
import { AdminReportesComponent } from './modules/admin/pages/admin-reportes/admin-reportes';
import { MiPerfilComponent } from './modules/admin/pages/mi-perfil/mi-perfil.component';
import { GestionPersonalComponent } from './modules/admin/gestion-personal/gestion-personal.component';
import { CatalogoQuejasComponent } from './modules/admin/catalogo-quejas/catalogo-quejas.component';

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
      { path: '', redirectTo: 'login', pathMatch: 'full' },
    ],
  },

  // Dashboard: Ciudadano
  {
    path: 'ciudadano',
    component: DashboardLayoutComponent,
    children: [
      { path: 'mis-quejas', component: MisQuejasComponent },
      { path: 'nueva-queja', component: CrearQuejaComponent },
      { path: 'perfil', component: MiPerfilComponent },
      { path: '', redirectTo: 'mis-quejas', pathMatch: 'full' },
    ],
  },

  // Dashboard: Funcionario Municipal
  {
    path: 'funcionario',
    component: DashboardLayoutComponent,
    children: [
      { path: 'bandeja', component: BandejaGestionComponent },
      { path: 'detalle-gestion/:id', component: DetalleQuejaGestionComponent },
      { path: '', redirectTo: 'bandeja', pathMatch: 'full' },
    ],
  },

  // Dashboard: Inspector de Campo
  {
    path: 'inspector',
    component: DashboardLayoutComponent,
    children: [
      { path: 'inicio', component: InicioInspectorComponent },
      { path: 'inspecciones', component: BandejaInspectorComponent },
      { path: 'detalle-inspector/:id', component: DetalleQuejaInspectorComponent },
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
    ],
  },

  // Dashboard: Especialista Técnico
  {
    path: 'especialista',
    component: DashboardLayoutComponent,
    children: [
      { path: 'inicio', component: InicioEspecialistaComponent },
      { path: 'ordenes', component: BandejaEspecialistaComponent },
      { path: 'detalle-especialista/:id', component: DetalleQuejaEspecialistaComponent },
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
    ],
  },

  // Dashboard: Administrador
  {
    path: 'admin',
    component: DashboardLayoutComponent,
    children: [
      { path: '', redirectTo: 'gestion-personal', pathMatch: 'full' },
      { path: 'gestion-personal', component: GestionPersonalComponent },
      { path: 'catalogo-quejas', component: CatalogoQuejasComponent },
      { path: 'reportes', component: AdminReportesComponent },
      // Mantenemos 'usuarios' por compatibilidad si alguna otra parte del código aún lo llama
      { path: 'usuarios', redirectTo: 'gestion-personal', pathMatch: 'full' },
    ],
  },

  // Fallback
  { path: '**', redirectTo: '' },
];
