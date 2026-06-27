import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../admin.service';
import { DashboardStats } from '../../admin.models';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class AdminDashboard implements OnInit {
  private admin = inject(AdminService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly stats = signal<DashboardStats | null>(null);

  /** Selected reference date, as YYYY-MM-DD. Defaults to today. */
  readonly selectedDate = signal(this.toIso(new Date()));

  // The backend does all the revenue maths; the component only reads it back.
  readonly dailyRevenue = computed(() => this.stats()?.dailyRevenue ?? 0);
  readonly monthlyRevenue = computed(() => this.stats()?.monthlyRevenue ?? 0);
  readonly annualRevenue = computed(() => this.stats()?.annualRevenue ?? 0);

  /** Trend bars from the server, with a display label derived from each date. */
  readonly trend = computed(() =>
    (this.stats()?.trend ?? []).map((t) => ({ ...t, label: this.shortLabel(t.date) }))
  );

  ngOnInit(): void {
    this.load();
  }

  onDateChange(value: string): void {
    if (value) {
      this.selectedDate.set(value);
      this.load();
    }
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.admin.getDashboardStats(this.selectedDate()).subscribe({
      next: (s) => { this.stats.set(s); this.loading.set(false); },
      error: () => { this.error.set('Could not load the dashboard.'); this.loading.set(false); },
    });
  }

  // ----- date display helpers (formatting only) -----

  private toIso(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  private shortLabel(iso: string): string {
    const [y, m, d] = iso.split('-').map(Number);
    return new Date(y, (m || 1) - 1, d || 1)
      .toLocaleDateString(undefined, { day: 'numeric', month: 'short' });
  }
}
