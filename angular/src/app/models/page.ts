// Abbild einer Spring Data Page<T>
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;       // aktuelle Seite (0-basiert)
  size: number;         // Seitengröße
}
