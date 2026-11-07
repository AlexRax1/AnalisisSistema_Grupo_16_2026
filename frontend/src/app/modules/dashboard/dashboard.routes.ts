import { Routes } from '@angular/router';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    component: AdminDashboardComponent,
  },
];
