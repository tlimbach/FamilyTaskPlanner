import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Benutzer } from '../models/benutzer';
import { BenutzerService } from '../services/benutzer.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Benutzer</h1>

    <div style="margin-bottom: .75rem;">
      <input [(ngModel)]="q" placeholder="Suche nach Name..." />
      <button (click)="search()">Suchen</button>
    </div>

    <div *ngIf="total() === 0; else listTpl">Keine Daten.</div>

    <ng-template #listTpl>
      <table style="width:100%; border-collapse:collapse;">
        <thead>
          <tr>
            <th style="text-align:left; border-bottom:1px solid #ddd; padding:.4rem;">Name</th>
            <th style="text-align:left; border-bottom:1px solid #ddd; padding:.4rem;">Farbe</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let u of users()">
            <td style="padding:.4rem;">{{ u.name }}</td>
            <td style="padding:.4rem;">
              <span [style.background]="u.farbe"
                    style="display:inline-block; width:1.2rem; height:1.2rem; border:1px solid #ccc; vertical-align:middle; margin-right:.5rem;"></span>
              <code>{{ u.farbe }}</code>
            </td>
          </tr>
        </tbody>
      </table>

      <div style="margin-top:.75rem; display:flex; gap:.5rem; align-items:center;">
        <button (click)="prev()" [disabled]="page() === 0">Zurück</button>
        <span>Seite {{ page() + 1 }} / {{ totalPages() }}</span>
        <button (click)="next()" [disabled]="page() + 1 >= totalPages()">Weiter</button>
        <span style="margin-left:.5rem;">(gesamt: {{ total() }})</span>
      </div>
    </ng-template>
  `,
  styles: [``]
})
export class UsersComponent implements OnInit {

  page   = signal(0);
  size   = signal(20);
  q: string = '';

  users = signal<Benutzer[]>([]);
  total = signal(0);
  totalPages = signal(1);

  constructor(private service: BenutzerService) {}

  ngOnInit(): void {
    this.load();
  }

  search(): void {
    this.page.set(0);
    this.load();
  }

  next(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.page.set(this.page() + 1);
      this.load();
    }
  }

  prev(): void {
    if (this.page() > 0) {
      this.page.set(this.page() - 1);
      this.load();
    }
  }

private load(): void {
  const pg = this.page();
  const sz = this.size();
  const term = this.q ?? '';
  console.log('[Users] load() → calling service.page', { page: pg, size: sz, q: term });

  this.service.page(pg, sz, term).subscribe({
    next: (p: any) => {
      console.log('[Users] service returned', p);

      // 1) gar nichts?
      if (!p) {
        console.warn('[Users] WARNING: page payload is null/undefined');
        this.users.set([]);
        this.total.set(0);
        this.totalPages.set(1);
        return;
      }

      // 2) Es kommt ein Array statt Page<T> (z.B. wenn Service falsch typisiert ist)
      if (Array.isArray(p)) {
        console.warn('[Users] WARNING: service returned an ARRAY, expected a Page<T>. Mapping array to table.');
        this.users.set(p);
        this.total.set(p.length);
        this.totalPages.set(1);
        return;
      }

      // 3) Objekt ohne "content" → auch laut schreien
      if (!('content' in p)) {
        console.warn('[Users] WARNING: object has no "content" property. Keys =', Object.keys(p));
        this.users.set([]);
        this.total.set(0);
        this.totalPages.set(1);
        return;
      }

      // 4) Happy path
      this.users.set(p.content ?? []);
      this.total.set(p.totalElements ?? (p.content?.length ?? 0));
      this.totalPages.set(Math.max(1, p.totalPages ?? 1));
    },
    error: err => {
      console.error('[Users] load() failed', err);
      this.users.set([]);
      this.total.set(0);
      this.totalPages.set(1);
    }
  });
}
}
