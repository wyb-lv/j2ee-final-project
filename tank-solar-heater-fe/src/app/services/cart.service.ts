import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Product } from '../models/catalog.models';
import { CartPricing } from '../models/cart.models';

export type PricingState = 'idle' | 'loading' | 'ok' | 'error';

/**
 * Shopping cart stored in the backend HTTP session. This service is a thin
 * client: every mutation hits /api/cart (with credentials so the JSESSIONID
 * cookie is sent) and the returned, server-priced cart becomes the UI state.
 * Nothing is kept in localStorage/sessionStorage on the client.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private base = environment.apiBase;

  // Send the session cookie on every cart call.
  private readonly opts = { withCredentials: true } as const;

  readonly cart = signal<CartPricing | null>(null);
  readonly open = signal(false);
  readonly pricingState = signal<PricingState>('idle');

  readonly lines = computed(() => this.cart()?.lines ?? []);
  readonly count = computed(() => this.cart()?.itemCount ?? 0);
  readonly total = computed(() => this.cart()?.total ?? 0);

  constructor() {
    this.load();
  }

  /** Loads the current session cart from the server. */
  load(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.pricingState.set('loading');
    this.http.get<CartPricing>(`${this.base}/cart`, this.opts).subscribe({
      next: (c) => this.apply(c),
      error: () => this.pricingState.set('error'),
    });
  }

  add(product: Product, qty = 1): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.pricingState.set('loading');
    this.http
      .post<CartPricing>(`${this.base}/cart/items`, { productId: product.id, quantity: qty }, this.opts)
      .subscribe({
        // Update the cart silently; the panel only opens when the user clicks the cart icon.
        next: (c) => this.apply(c),
        error: () => this.pricingState.set('error'),
      });
  }

  setQty(productId: number, qty: number): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.pricingState.set('loading');
    this.http
      .put<CartPricing>(`${this.base}/cart/items/${productId}`, { quantity: qty }, this.opts)
      .subscribe({ next: (c) => this.apply(c), error: () => this.pricingState.set('error') });
  }

  remove(productId: number): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.pricingState.set('loading');
    this.http
      .delete<CartPricing>(`${this.base}/cart/items/${productId}`, this.opts)
      .subscribe({ next: (c) => this.apply(c), error: () => this.pricingState.set('error') });
  }

  clear(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.pricingState.set('loading');
    this.http
      .delete<CartPricing>(`${this.base}/cart`, this.opts)
      .subscribe({ next: (c) => this.apply(c), error: () => this.pricingState.set('error') });
  }

  openCart(): void {
    this.open.set(true);
    this.load();
  }
  closeCart(): void {
    this.open.set(false);
  }
  toggleCart(): void {
    this.open.update((v) => !v);
  }

  private apply(c: CartPricing): void {
    this.cart.set(c);
    this.pricingState.set('ok');
  }
}
