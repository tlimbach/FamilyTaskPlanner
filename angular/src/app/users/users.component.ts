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
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
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
        const merged = reset ? [...content] : [...this.users(), ...content];
        merged.sort((a, b) => a.name.localeCompare(b.name, 'de', { sensitivity: 'base', numeric: true }));
        this.users.set(merged);

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
