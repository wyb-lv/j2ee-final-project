import { Component, OnInit, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../services/catalog.service';
import { Category, Product } from '../../models/catalog.models';
import { ProductCard } from '../../components/product-card/product-card';

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
  readonly products = signal<Product[]>([]);
  readonly loading = signal(false);
  readonly failed = signal(false);

  readonly page = signal(1);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);

  // filter state
  keyword = '';
  selectedCategory = signal<number | null>(null);
  sort = signal('');

  readonly pages = computed(() =>
    Array.from({ length: this.totalPages() }, (_, i) => i + 1)
  );

  readonly sortOptions = [
    { value: '', label: 'Featured' },
    { value: 'price,asc', label: 'Price: Low to High' },
    { value: 'price,desc', label: 'Price: High to Low' },
    { value: 'name,asc', label: 'Name: A–Z' },
  ];

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.catalog.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
      error: () => {},
    });
    this.load();
  }

  load(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.loading.set(true);
    this.failed.set(false);
    this.catalog
      .getProducts({
        page: this.page(),
        keyword: this.keyword.trim() || undefined,
        categoryId: this.selectedCategory() ?? undefined,
        sort: this.sort() || undefined,
      })
      .subscribe({
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

  applyFilters(): void {
    this.page.set(1);
    this.load();
  }

  onSearch(): void {
    this.applyFilters();
  }

  selectCategory(id: number | null): void {
    this.selectedCategory.set(id);
    this.applyFilters();
  }

  onSortChange(value: string): void {
    this.sort.set(value);
    this.applyFilters();
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
    this.keyword = '';
    this.selectedCategory.set(null);
    this.sort.set('');
    this.applyFilters();
  }
}
