import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Benutzer } from '../models/benutzer';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // current page index
}

@Injectable({ providedIn: 'root' })
export class BenutzerService {
  private http = inject(HttpClient);

  page(page: number, size: number, q?: string): Observable<Page<Benutzer>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (q && q.trim().length > 0) params = params.set('q', q.trim());

    return this.http
      .get<Page<Benutzer>>('/api/users', { params })
      .pipe(tap(resp => console.log('[BenutzerService] GET /api/users →', resp)));
  }
}
