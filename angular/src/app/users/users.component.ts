import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ScrollingModule, CdkVirtualScrollViewport } from '@angular/cdk/scrolling';
import { Benutzer } from '../models/benutzer';
import { BenutzerService } from '../services/benutzer.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollingModule],
  template: `
    <h1>Benutzer</h1>

    <div style="margin-bottom:.75rem; display:flex; gap:.5rem; align-items:center;">
      <input [(ngModel)]="q" placeholder="Suche nach Name..." />
      <button (click)="onSearch()">Suchen</button>
      <span *ngIf="loading()" style="opacity:.7;">lädt …</span>
    </div>

    <div *ngIf="total() === 0 && !loading(); else listTpl">Keine Daten.</div>

    <ng-template #listTpl>
      <!-- Virtual Scroll Viewport: lädt nach, sobald wir ans Ende kommen -->
      <cdk-virtual-scroll-viewport
        #viewport
        [itemSize]="36"
        class="viewport"
        (scrolledIndexChange)="onScrolled($event)">

        <div class="row header">
          <div class="cell name"><b>Name</b></div>
          <div class="cell color"><b>Farbe</b></div>
        </div>

        <div *cdkVirtualFor="let u of users(); trackBy: trackById" class="row">
          <div class="cell name">{{ u.name }}</div>
          <div class="cell color">
            <span class="chip" [style.background]="u.farbe"></span>
            <code>{{ u.farbe }}</code>
          </div>
        </div>

        <div class="row footer" *ngIf="loading()">Weitere Daten werden geladen …</div>
      </cdk-virtual-scroll-viewport>
    </ng-template>
  `,
  styles: [`
    .viewport { height: 70vh; width: 100%; border: 1px solid #ddd; border-radius: 6px; }
    .row { display: grid; grid-template-columns: 1fr 220px; align-items: center; height: 36px; box-sizing: border-box; }
    .header { position: sticky; top: 0; background: #fafafa; border-bottom: 1px solid #e5e5e5; z-index: 1; }
    .cell { padding: 0 .6rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .name { }
    .color { display: flex; gap: .5rem; align-items: center; }
    .chip { display:inline-block; width: 18px; height: 18px; border:1px solid #ccc; border-radius:4px; }
    .footer { justify-content: center; font-style: italic; color: #666; }
  `]
})
export class UsersComponent implements OnInit {

  // Query & Paging-Status
  q = '';
  page = signal(0);
  size = signal(100);       // größere Seiten -> weniger Netz-Overhead
  total = signal(0);
  totalPages = signal(1);

  users = signal<Benutzer[]>([]);
  loading = signal(false);
  finished = computed(() => this.page() + 1 >= this.totalPages());

  constructor(private service: BenutzerService) {}

  ngOnInit(): void {
    this.resetAndLoad();
  }

  // === UI Events ===
  onSearch(): void {
    this.resetAndLoad();
  }

  onScrolled(idx: number): void {
    // Wenn der sichtbare Index nahe am Ende ist, nächste Seite laden
    const threshold = Math.max(5, Math.floor(this.size() * 0.2)); // z.B. letzte 20% oder mind. 5
    if (!this.loading() && !this.finished() && idx + threshold >= this.users().length) {
      this.loadNextPage();
    }
  }

  trackById(index: number, u: Benutzer) { return u.id; }

  // === Lade-Logik ===
  private resetAndLoad(): void {
    this.page.set(0);
    this.total.set(0);
    this.totalPages.set(1);
    this.users.set([]);
    this.loadNextPage(true);
  }

  private loadNextPage(reset = false): void {
    this.loading.set(true);

    const nextPage = reset ? 0 : this.page();
    this.service.page(nextPage, this.size(), this.q).subscribe({
      next: p => {
        const content = p?.content ?? [];
        const total = p?.totalElements ?? 0;
        const totalPages = Math.max(1, p?.totalPages ?? 1);

        // an bestehende Liste anhängen (oder ersetzen bei reset)
        if (reset) {
          this.users.set(content);
        } else {
          this.users.set([...this.users(), ...content]);
        }

        this.total.set(total);
        this.totalPages.set(totalPages);

        // nur erhöhen, wenn wirklich noch was kam
        if (content.length > 0 && nextPage < totalPages) {
          this.page.set(nextPage + 1);
        }
        this.loading.set(false);
      },
      error: err => {
        console.error('[Users] load failed', err);
        this.loading.set(false);
      }
    });
  }
}
