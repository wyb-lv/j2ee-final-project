import { Component, Input, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product } from '../../models/catalog.models';
import { CartService, priceAfterDiscount } from '../../services/cart.service';

@Component({
  selector: 'app-product-card',
  imports: [CommonModule],
  templateUrl: './product-card.html',
  styleUrl: './product-card.css',
})
export class ProductCard {
  private cart = inject(CartService);
  private _product = signal<Product | null>(null);

  @Input({ required: true })
  set product(value: Product) {
    this._product.set(value);
  }
  get product(): Product {
    return this._product()!;
  }

  readonly hasDiscount = computed(() => Number(this._product()?.discount ?? 0) > 0);
  readonly finalPrice = computed(() => {
    const p = this._product();
    return p ? priceAfterDiscount(p) : 0;
  });

  readonly placeholder =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(
      `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300"><rect width="400" height="300" fill="#f6f8fb"/><text x="50%" y="50%" font-family="Manrope,sans-serif" font-size="20" fill="#9aa3b2" text-anchor="middle" dominant-baseline="middle">No image</text></svg>`
    );

  addToCart(): void {
    const p = this._product();
    if (p) this.cart.add(p);
  }

  onImgError(ev: Event): void {
    (ev.target as HTMLImageElement).src = this.placeholder;
  }
}
