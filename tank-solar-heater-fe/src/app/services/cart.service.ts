import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Product } from '../models/catalog.models';
import { CartLine, CartPricing } from '../models/cart.models';

export interface CartItem {
  product: Product;
  qty: number;
}

export type PricingState = 'idle' | 'loading' | 'ok' | 'error';

const STORAGE_KEY = 'suntank_cart';

export function priceAfterDiscount(p: Product): number {
  const discount = Number(p.discount ?? 0);
  return Number(p.price) * (1 - discount / 100);
}

/**
 * Client-side shopping cart. State lives entirely in the browser's
 * localStorage — it is never persisted to the backend database.
 *
 * Totals are validated server-side via POST /api/cart (the stateless backend
 * cart module). If the server can't be reached, the locally computed totals
 * are used as a fallback so the UI keeps working.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private platformId = inject(PLATFORM_ID);
  private http = inject(HttpClient);
  private base = environment.apiBase;

  readonly items = signal<CartItem[]>(this.restore());
  readonly open = signal(false);

  // Server-validated pricing (null until validated / when cart is empty).
  readonly priced = signal<CartPricing | null>(null);
  readonly pricingState = signal<PricingState>('idle');

  readonly count = computed(() => this.items().reduce((n, i) => n + i.qty, 0));

  /** Local fallback total (used until/unless the server responds). */
  readonly localTotal = computed(() =>
    this.items().reduce((sum, i) => sum + priceAfterDiscount(i.product) * i.qty, 0)
  );

  /** Authoritative total: server value when available, else local. */
  readonly total = computed(() => this.priced()?.total ?? this.localTotal());

  /** Server-priced lines keyed by productId, for per-line display. */
  readonly pricedLines = computed(() => {
    const map = new Map<number, CartLine>();
    this.priced()?.lines.forEach((l) => map.set(l.productId, l));
    return map;
  });

  private revalidateTimer?: ReturnType<typeof setTimeout>;

  constructor() {
    this.scheduleRevalidate();
  }

  add(product: Product, qty = 1): void {
    this.items.update((items) => {
      const existing = items.find((i) => i.product.id === product.id);
      if (existing) {
        return items.map((i) =>
          i.product.id === product.id ? { ...i, qty: i.qty + qty } : i
        );
      }
      return [...items, { product, qty }];
    });
    this.persist();
    this.open.set(true);
    this.scheduleRevalidate();
  }

  setQty(id: number, qty: number): void {
    if (qty <= 0) return this.remove(id);
    this.items.update((items) =>
      items.map((i) => (i.product.id === id ? { ...i, qty } : i))
    );
    this.persist();
    this.scheduleRevalidate();
  }

  remove(id: number): void {
    this.items.update((items) => items.filter((i) => i.product.id !== id));
    this.persist();
    this.scheduleRevalidate();
  }

  clear(): void {
    this.items.set([]);
    this.persist();
    this.priced.set(null);
    this.pricingState.set('idle');
  }

  /** Line total: prefers the server-validated value, falls back to local. */
  lineTotal(product: Product, qty: number): number {
    const line = this.pricedLines().get(product.id);
    return line ? line.lineTotal : priceAfterDiscount(product) * qty;
  }

  openCart(): void {
    this.open.set(true);
    this.scheduleRevalidate();
  }
  closeCart(): void {
    this.open.set(false);
  }
  toggleCart(): void {
    this.open.update((v) => !v);
  }

  /** Debounced server revalidation so rapid qty changes don't spam the API. */
  scheduleRevalidate(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    clearTimeout(this.revalidateTimer);
    this.revalidateTimer = setTimeout(() => this.revalidate(), 300);
  }

  revalidate(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    const items = this.items();
    if (items.length === 0) {
      this.priced.set(null);
      this.pricingState.set('idle');
      return;
    }
    this.pricingState.set('loading');
    this.http
      .post<CartPricing>(`${this.base}/cart`, {
        items: items.map((i) => ({ productId: i.product.id, quantity: i.qty })),
      })
      .subscribe({
        next: (pricing) => {
          this.priced.set(pricing);
          this.pricingState.set('ok');
        },
        error: () => {
          // Keep showing local totals; flag that server prices are unavailable.
          this.pricingState.set('error');
        },
      });
  }

  private persist(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.items()));
    } catch {
      /* ignore quota / unavailable */
    }
  }

  private restore(): CartItem[] {
    if (!isPlatformBrowser(this.platformId)) return [];
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as CartItem[]) : [];
    } catch {
      return [];
    }
  }
}
