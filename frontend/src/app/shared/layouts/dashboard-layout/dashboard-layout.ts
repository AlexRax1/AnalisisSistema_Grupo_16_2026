import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { GestionPersonalComponent } from '../../../modules/admin/gestion-personal/gestion-personal.component';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

interface RolConfig {
  titulo: string;
  badgeClass: string;
  items: NavItem[];
}

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, GestionPersonalComponent],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.css',
})
export class DashboardLayoutComponent implements OnInit {
  sidebarCollapsed = false;
  rolActual: string = '';
  nombreUsuario: string = 'Usuario';

  rolConfigs: Record<string, RolConfig> = {
    ciudadano: {
      titulo: 'Portal Ciudadano',
      badgeClass: 'badge-primary',
      items: [
        { label: 'Mis Quejas', icon: 'list_alt', route: '/ciudadano/mis-quejas' },
        { label: 'Nueva Queja', icon: 'add_circle', route: '/ciudadano/nueva-queja' },
        { label: 'Mi Perfil', route: '/ciudadano/perfil', icon: 'person' },
      ],
    },
    funcionario: {
      titulo: 'Gestión Municipal',
      badgeClass: 'badge-warning',
      items: [{ label: 'Bandeja General', icon: 'inbox', route: '/funcionario/bandeja' }],
    },
    inspector: {
      titulo: 'Inspector de Campo',
      badgeClass: 'badge-info',
      items: [
        { label: 'Inicio', icon: 'home', route: '/inspector/inicio' },
        { label: 'Inspecciones', icon: 'search', route: '/inspector/inspecciones' },
      ],
    },
    especialista: {
      titulo: 'Especialista Técnico',
      badgeClass: 'badge-success',
      items: [
        { label: 'Inicio', icon: 'home', route: '/especialista/inicio' },
        { label: 'Órdenes de Trabajo', icon: 'build', route: '/especialista/ordenes' },
      ],
    },
    admin: {
      titulo: 'Administrador',
      badgeClass: 'badge-danger',
      items: [
        // 👈 La ruta del menú apunta correctamente a gestion-personal
        { label: 'Gestión de Personal', icon: 'people', route: '/admin/gestion-personal' },
        { label: 'Catálogo de Quejas', icon: 'assessment', route: '/admin/catalogo-quejas' },
        { label: 'Reportes', icon: 'bar_chart', route: '/admin/reportes' },
      ],
    },
  };

  currentConfig: RolConfig | null = null;

  constructor(private router: Router) {}

  ngOnInit() {
    this.detectarRol();
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => this.detectarRol());
  }

  private detectarRol() {
    const url = this.router.url;
    const segmento = url.split('/')[1] || 'ciudadano'; // ciudadano, funcionario, inspector, admin, etc.
    this.currentConfig = this.rolConfigs[segmento] || null;

    // Leer rol del localStorage
    const rolStorage = localStorage.getItem('rol');
    if (rolStorage) {
      this.rolActual = rolStorage;
    } else {
      this.rolActual = segmento.toUpperCase();
    }
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  cerrarSesion() {
    localStorage.removeItem('token');
    localStorage.removeItem('rol');
    this.router.navigate(['/auth/login']);
  }
}
