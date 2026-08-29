export interface CategoriaQueja {
  id: number;
  nombre: string;
  subcategorias: SubcategoriaQueja[];
}

export interface SubcategoriaQueja {
  id: number;
  nombre: string;
}

export const CATEGORIAS_QUEJA: CategoriaQueja[] = [
  {
    id: 1,
    nombre: 'Alumbrado Público',
    subcategorias: [
      { id: 1, nombre: 'Luminaria apagada / quemada' },
      { id: 2, nombre: 'Poste inclinado o dañado' },
      { id: 3, nombre: 'Cableado expuesto' }
    ]
  },
  {
    id: 2,
    nombre: 'Drenajes y Alcantarillado',
    subcategorias: [
      { id: 4, nombre: 'Tragante obstruido' },
      { id: 5, nombre: 'Inundación por drenaje' },
      { id: 6, nombre: 'Falta de tapadera de alcantarilla' },
      { id: 7, nombre: 'Mal olor proveniente de drenaje' }
    ]
  },
  {
    id: 3,
    nombre: 'Vialidad y Espacios Públicos',
    subcategorias: [
      { id: 8, nombre: 'Bache en calle' },
      { id: 9, nombre: 'Señalización vial dañada' },
      { id: 10, nombre: 'Banqueta dañada' },
      { id: 11, nombre: 'Obstáculo en vía pública' }
    ]
  },
  {
    id: 4,
    nombre: 'Limpieza y Áreas Verdes',
    subcategorias: [
      { id: 12, nombre: 'Acumulación de basura en vía pública' },
      { id: 13, nombre: 'Basurero clandestino' },
      { id: 14, nombre: 'Problema con recolección de residuos' },
      { id: 15, nombre: 'Árbol con riesgo de caída' }
    ]
  }
];

export interface RegistroQuejaPayload {
  categoriaId: number;
  subcategoriaId: number;
  zona: number; // Por compatibilidad con backend enviamos 1 o 0 si no aplica zona municipal
  direccionExacta: string;
  puntoReferencia: string;
  latitud: number;
  longitud: number;
  descripcion: string;
}

export interface RespuestaRegistroQueja {
  correlativo: string;
  mensaje: string;
}
