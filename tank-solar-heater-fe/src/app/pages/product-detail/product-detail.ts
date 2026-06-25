import { Component, OnInit, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CatalogService } from '../../services/catalog.service';
import { Product } from '../../models/catalog.models';
import { CartService, priceAfterDiscount } from '../../services/cart.service';
import { ProductCard } from '../../components/product-card/product-card';
import { resolveImageUrl } from '../../shared/image.util';

@Component({
  selector: 'app-product-detail',
  imports: [CommonModule, RouterLink, ProductCard],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.css',
})
export class ProductDetail implements OnInit {
  private catalog = inject(CatalogService);
  private cart = inject(CartService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  readonly product = signal<Product | null>(null);
  readonly related = signal<Product[]>([]);
  readonly loading = signal(true);
  readonly failed = signal(false);
  readonly qty = signal(1);
  readonly added = signal(false);

  readonly resolveImageUrl = resolveImageUrl;

  readonly hasDiscount = computed(() => Number(this.product()?.discount ?? 0) > 0);
  readonly finalPrice = computed(() => {
    const p = this.product();
    return p ? priceAfterDiscount(p) : 0;
  });
  readonly savings = computed(() => {
    const p = this.product();
    if (!p || !this.hasDiscount()) return 0;
    return (Number(p.price) - this.finalPrice()) * this.qty();
  });
  readonly lineTotal = computed(() => this.finalPrice() * this.qty());

  // Static selling points — mirrors the Shopify "trust badges" / highlights blocks.
  readonly badges = [
    { icon: '🚚', text: 'Free delivery & installation' },
    { icon: '🛡️', text: '5-year manufacturer warranty' },
    { icon: '🔒', text: 'Secure checkout' },
  ];
  readonly highlights = [
    'High-efficiency thermal collector for year-round hot water',
    'Corrosion-resistant tank with premium insulation',
    'Low running cost — cuts water-heating energy use',
    'Professional installation by certified technicians',
  ];

  readonly placeholder =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(
      `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="600"><rect width="600" height="600" fill="#f6f8fb"/><text x="50%" y="50%" font-family="Manrope,sans-serif" font-size="24" fill="#9aa3b2" text-anchor="middle" dominant-baseline="middle">No image</text></svg>`
    );

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = Number(params.get('id'));
      if (Number.isFinite(id) && id > 0) this.load(id);
      else this.failed.set(true);
    });
  }

  private load(id: number): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.loading.set(true);
    this.failed.set(false);
    this.qty.set(1);
    this.product.set(null);
    this.related.set([]);
    if (isPlatformBrowser(this.platformId)) window.scrollTo({ top: 0, behavior: 'auto' });

    this.catalog.getProduct(id).subscribe({
      next: (p) => {
        this.product.set(p);
        this.loading.set(false);
        this.loadRelated(p);
      },
      error: () => {
        this.failed.set(true);
        this.loading.set(false);
      },
    });
  }

  private loadRelated(p: Product): void {
    this.catalog.getProducts({ categoryId: p.categoryId, page: 1 }).subscribe({
      next: (pg) => {
        this.related.set(pg.content.filter((x) => x.id !== p.id).slice(0, 4));
      },
      error: () => this.related.set([]),
    });
  }

  decQty(): void {
    this.qty.update((q) => Math.max(1, q - 1));
  }
  incQty(): void {
    this.qty.update((q) => Math.min(99, q + 1));
  }

  addToCart(): void {
    const p = this.product();
    if (!p) return;
    this.cart.add(p, this.qty());
    this.added.set(true);
    setTimeout(() => this.added.set(false), 1800);
  }

  buyNow(): void {
    const p = this.product();
    if (!p) return;
    this.cart.add(p, this.qty());
    this.router.navigate(['/checkout']);
  }

  onImgError(ev: Event): void {
    (ev.target as HTMLImageElement).src = this.placeholder;
  }
}
