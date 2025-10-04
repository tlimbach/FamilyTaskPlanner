export interface Benutzer {
  id: number;
  name: string;
  farbe: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;      // aktuelle Seite (0-basiert)
  size: number;        // Seitengröße
}
