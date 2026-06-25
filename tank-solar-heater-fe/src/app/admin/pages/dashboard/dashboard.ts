import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../admin.service';
import { OrderResponse } from '../../../models/checkout.models';

interface TrendBar {
  date: string;   // YYYY-MM-DD
  label: string;  // short day/month label for the axis
  total: number;
  pct: number;    // bar height as % of the tallest bar in the window
}

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class AdminDashboard implements OnInit {
  private admin = inject(AdminService);

  readonly loading = signal(true);
  readonly productCount = signal(0);
  readonly categoryCount = signal(0);
  readonly brandCount = signal(0);
  readonly orders = signal<OrderResponse[]>([]);

  /** Selected reference date, as YYYY-MM-DD. Defaults to today. */
  readonly selectedDate = signal(this.toIso(new Date()));

  /** Revenue of the selected day. */
  readonly dailyRevenue = computed(() => {
    const d = this.selectedDate();
    return this.sum((o) => this.day(o.date) === d);
  });

  /** Revenue of the month containing the selected date (YYYY-MM prefix). */
  readonly monthlyRevenue = computed(() => {
    const ym = this.selectedDate().slice(0, 7);
    return this.sum((o) => this.day(o.date).startsWith(ym));
  });

  /** Revenue of the year containing the selected date (YYYY prefix). */
  readonly annualRevenue = computed(() => {
    const y = this.selectedDate().slice(0, 4);
    return this.sum((o) => this.day(o.date).startsWith(y));
  });

  /** Revenue for the 7 consecutive days ending on (and including) the selected date. */
  readonly trend = computed<TrendBar[]>(() => {
    const end = this.parseIso(this.selectedDate());
    const days: { date: string; total: number }[] = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date(end);
      d.setDate(end.getDate() - i);
      const iso = this.toIso(d);
      days.push({ date: iso, total: this.sum((o) => this.day(o.date) === iso) });
    }
    const max = Math.max(1, ...days.map((d) => d.total));
    return days.map((d) => ({
      date: d.date,
      label: this.shortLabel(d.date),
      total: d.total,
      pct: Math.round((d.total / max) * 100),
    }));
  });

  ngOnInit(): void {
    this.admin.listProducts({ page: 1 }).subscribe({ next: (p) => this.productCount.set(p.totalElements) });
    this.admin.listCategories().subscribe({ next: (c) => this.categoryCount.set(c.length) });
    this.admin.listBrands().subscribe({ next: (b) => this.brandCount.set(b.length) });
    this.admin.listOrders().subscribe({
      next: (o) => { this.orders.set(o); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  onDateChange(value: string): void {
    if (value) this.selectedDate.set(value);
  }

  // ----- helpers -----

  /** Sums order totals for orders matching the predicate. */
  private sum(match: (o: OrderResponse) => boolean): number {
    return this.orders()
      .filter(match)
      .reduce((s, o) => s + Number(o.total ?? 0), 0);
  }

  /** Normalises an order date (which may carry a time component) to YYYY-MM-DD. */
  private day(date: string): string {
    return (date ?? '').slice(0, 10);
  }

  private toIso(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  private parseIso(iso: string): Date {
    const [y, m, d] = iso.split('-').map(Number);
    return new Date(y, (m || 1) - 1, d || 1);
  }

  private shortLabel(iso: string): string {
    const d = this.parseIso(iso);
    return d.toLocaleDateString(undefined, { day: 'numeric', month: 'short' });
  }
}
