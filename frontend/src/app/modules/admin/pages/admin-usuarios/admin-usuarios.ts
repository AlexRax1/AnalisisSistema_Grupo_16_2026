import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface UsuarioInterno {
  id: number;
  nombre: string;
  correo: string;
  rol: string;
  estado: 'ACTIVO' | 'INACTIVO';
  fechaCreacion: Date;
}

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-usuarios.html',
  styleUrl: './admin-usuarios.css'
})
export class AdminUsuariosComponent {
  usuarios: UsuarioInterno[] = [
    { id: 1, nombre: 'Carlos Gómez', correo: 'carlos.gomez@muni.gob.gt', rol: 'INSPECTOR', estado: 'ACTIVO', fechaCreacion: new Date('2026-03-15') },
    { id: 2, nombre: 'María Rodríguez', correo: 'maria.rdz@muni.gob.gt', rol: 'INSPECTOR', estado: 'ACTIVO', fechaCreacion: new Date('2026-04-10') },
    { id: 3, nombre: 'Jorge Morales', correo: 'jorge.m@muni.gob.gt', rol: 'ESPECIALISTA', estado: 'ACTIVO', fechaCreacion: new Date('2026-05-20') },
    { id: 4, nombre: 'Ana López', correo: 'ana.lopez@muni.gob.gt', rol: 'FUNCIONARIO', estado: 'ACTIVO', fechaCreacion: new Date('2026-02-01') },
    { id: 5, nombre: 'Pedro Castillo', correo: 'pedro.c@muni.gob.gt', rol: 'ESPECIALISTA', estado: 'INACTIVO', fechaCreacion: new Date('2026-01-10') }
  ];

  usuariosFiltrados: UsuarioInterno[] = [...this.usuarios];
  filtroRol: string = 'TODOS';
  busqueda: string = '';
  mostrarModal: boolean = false;
  editando: boolean = false;

  nuevoUsuario: Partial<UsuarioInterno> = { nombre: '', correo: '', rol: 'INSPECTOR', estado: 'ACTIVO' };

  roles: string[] = ['FUNCIONARIO', 'INSPECTOR', 'ESPECIALISTA', 'ADMINISTRADOR'];

  aplicarFiltros() {
    this.usuariosFiltrados = this.usuarios.filter(u => {
      const cumpleRol = this.filtroRol === 'TODOS' || u.rol === this.filtroRol;
      const cumpleBusqueda = !this.busqueda ||
        u.nombre.toLowerCase().includes(this.busqueda.toLowerCase()) ||
        u.correo.toLowerCase().includes(this.busqueda.toLowerCase());
      return cumpleRol && cumpleBusqueda;
    });
  }

  abrirCrear() {
    this.editando = false;
    this.nuevoUsuario = { nombre: '', correo: '', rol: 'INSPECTOR', estado: 'ACTIVO' };
    this.mostrarModal = true;
  }

  editarUsuario(u: UsuarioInterno) {
    this.editando = true;
    this.nuevoUsuario = { ...u };
    this.mostrarModal = true;
  }

  guardarUsuario() {
    if (this.editando && this.nuevoUsuario.id) {
      const idx = this.usuarios.findIndex(u => u.id === this.nuevoUsuario.id);
      if (idx !== -1) this.usuarios[idx] = { ...this.usuarios[idx], ...this.nuevoUsuario } as UsuarioInterno;
    } else {
      const nuevo: UsuarioInterno = {
        id: this.usuarios.length + 1,
        nombre: this.nuevoUsuario.nombre || '',
        correo: this.nuevoUsuario.correo || '',
        rol: this.nuevoUsuario.rol || 'INSPECTOR',
        estado: 'ACTIVO',
        fechaCreacion: new Date()
      };
      this.usuarios.push(nuevo);
    }
    this.mostrarModal = false;
    this.aplicarFiltros();
  }

  toggleEstado(u: UsuarioInterno) {
    u.estado = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
  }
}
