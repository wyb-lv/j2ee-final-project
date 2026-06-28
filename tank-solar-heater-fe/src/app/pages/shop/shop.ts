import { Component, OnInit, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { CatalogService } from '../../services/catalog.service';
import { Brand, Category, Page, Product } from '../../models/catalog.models';
import { ProductCard } from '../../components/product-card/product-card';

/** Which single filter is currently driving the listing (filters are mutually exclusive). */
type FilterMode = 'all' | 'keyword' | 'category' | 'brand' | 'price';

@Component({
  selector: 'app-shop',
  imports: [CommonModule, FormsModule, ProductCard],
  templateUrl: './shop.html',
  styleUrl: './shop.css',
})
export class Shop implements OnInit {
  private catalog = inject(CatalogService);
  private platformId = inject(PLATFORM_ID);

  readonly categories = signal<Category[]>([]);
  readonly brands = signal<Brand[]>([]);
  readonly products = signal<Product[]>([]);
  readonly loading = signal(false);
  readonly failed = signal(false);

  readonly page = signal(1);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);

  // Only one filter is active at a time; `mode` decides which endpoint load() calls.
  readonly mode = signal<FilterMode>('all');
  keyword = '';
  selectedCategory = signal<number | null>(null);
  selectedBrand = signal<number | null>(null);
  minPrice: number | null = null;
  maxPrice: number | null = null;
  sort = signal('');

  /** Up to 3 page numbers, sliding so the current page sits in the middle when possible. */
  readonly pages = computed(() => {
    const total = this.totalPages();
    const windowSize = 3;
    if (total <= windowSize) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }
    const start = Math.min(Math.max(this.page() - 1, 1), total - windowSize + 1);
    return Array.from({ length: windowSize }, (_, i) => start + i);
  });

  readonly sortOptions = [
    { value: '', label: 'Nổi bật' },
    { value: 'price,asc', label: 'Giá: Thấp đến cao' },
    { value: 'price,desc', label: 'Giá: Cao đến thấp' },
    { value: 'name,asc', label: 'Tên: A–Z' },
  ];

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.catalog.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
      error: () => {},
    });
    this.catalog.getBrands().subscribe({
      next: (brands) => this.brands.set(brands),
      error: () => {},
    });
    this.load();
  }

  load(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.loading.set(true);
    this.failed.set(false);

    const q = { page: this.page(), sort: this.sort() || undefined };
    let request: Observable<Page<Product>>;

    switch (this.mode()) {
      case 'keyword':
        request = this.catalog.searchProducts(this.keyword.trim(), q);
        break;
      case 'category':
        request = this.catalog.getProductsByCategory(this.selectedCategory()!, q);
        break;
      case 'brand':
        request = this.catalog.getProductsByBrand(this.selectedBrand()!, q);
        break;
      case 'price':
        request = this.catalog.getProductsByPrice(this.minPrice, this.maxPrice, q);
        break;
      default:
        request = this.catalog.getAllProducts(q);
    }

    request.subscribe({
      next: (pg) => {
        this.products.set(pg.content);
        this.totalPages.set(pg.totalPages);
        this.totalElements.set(pg.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.failed.set(true);
        this.loading.set(false);
        this.products.set([]);
      },
    });
  }

  /** Switches to a filter mode, clears the other filters, and reloads from page 1. */
  private activate(mode: FilterMode): void {
    if (mode !== 'keyword') this.keyword = '';
    if (mode !== 'category') this.selectedCategory.set(null);
    if (mode !== 'brand') this.selectedBrand.set(null);
    if (mode !== 'price') { this.minPrice = null; this.maxPrice = null; }
    this.mode.set(mode);
    this.page.set(1);
    this.load();
  }

  onSearch(): void {
    this.activate(this.keyword.trim() ? 'keyword' : 'all');
  }

  selectCategory(id: number | null): void {
    this.selectedCategory.set(id);
    this.activate(id === null ? 'all' : 'category');
  }

  selectBrand(id: number | null): void {
    this.selectedBrand.set(id);
    this.activate(id === null ? 'all' : 'brand');
  }

  /** Applies the price range; ignores an inverted range (min > max). */
  applyPriceFilter(): void {
    if (this.minPrice == null && this.maxPrice == null) {
      this.activate('all');
      return;
    }
    if (this.minPrice != null && this.maxPrice != null && this.minPrice > this.maxPrice) {
      return;
    }
    this.activate('price');
  }

  onSortChange(value: string): void {
    // Sort re-applies within the current filter mode, page resets to 1.
    this.sort.set(value);
    this.page.set(1);
    this.load();
  }

  goToPage(p: number): void {
    if (p < 1 || p > this.totalPages() || p === this.page()) return;
    this.page.set(p);
    this.load();
    if (isPlatformBrowser(this.platformId)) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  clearAll(): void {
    this.sort.set('');
    this.activate('all');
  }
}
