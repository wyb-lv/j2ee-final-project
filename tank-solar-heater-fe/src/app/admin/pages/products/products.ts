import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../admin.service';
import { Category, Product } from '../../../models/catalog.models';
import { Brand, ProductRequest } from '../../admin.models';
import { resolveImageUrl } from '../../../shared/image.util';

@Component({
  selector: 'app-admin-products',
  imports: [CommonModule, FormsModule],
  templateUrl: './products.html',
  styleUrl: './products.css',
})
export class AdminProducts implements OnInit {
  private admin = inject(AdminService);

  readonly resolveImageUrl = resolveImageUrl;

  readonly categories = signal<Category[]>([]);
  readonly brands = signal<Brand[]>([]);
  readonly products = signal<Product[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly page = signal(1);
  readonly totalPages = signal(0);
  readonly categoryId = signal<number | null>(null);
  keyword = '';

  readonly modalOpen = signal(false);
  readonly editing = signal<Product | null>(null);
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);
  form: ProductRequest = this.emptyForm();

  // Image upload state
  readonly uploading = signal(false);
  readonly uploadError = signal<string | null>(null);
  readonly dragOver = signal(false);
  private readonly maxImageBytes = 5 * 1024 * 1024; // 5MB, matches backend limit

  readonly placeholder =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="46" height="46"><rect width="46" height="46" fill="#f6f8fb"/></svg>');

  ngOnInit(): void {
    this.admin.listCategories().subscribe((c) => this.categories.set(c));
    this.admin.listBrands().subscribe((b) => this.brands.set(b));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.admin
      .listProducts({ page: this.page(), keyword: this.keyword.trim() || undefined, categoryId: this.categoryId() ?? undefined })
      .subscribe({
        next: (pg) => { this.products.set(pg.content); this.totalPages.set(pg.totalPages); this.loading.set(false); },
        error: () => { this.error.set('Could not load products. Is the backend running?'); this.loading.set(false); },
      });
  }

  pageNumbers(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i + 1);
  }
  applyFilters(): void { this.page.set(1); this.load(); }
  onCategory(id: number | null): void { this.categoryId.set(id); this.applyFilters(); }
  goToPage(p: number): void {
    if (p < 1 || p > this.totalPages() || p === this.page()) return;
    this.page.set(p); this.load();
  }

  openCreate(): void {
    this.editing.set(null);
    this.form = this.emptyForm();
    this.formError.set(null);
    this.resetUpload();
    this.modalOpen.set(true);
  }
  openEdit(p: Product): void {
    this.editing.set(p);
    this.form = {
      name: p.name, description: p.description, price: Number(p.price), discount: Number(p.discount),
      categoryId: p.categoryId, brandId: p.brandId, imageUrl: p.imageUrl,
    };
    this.formError.set(null);
    this.resetUpload();
    this.modalOpen.set(true);
  }
  closeModal(): void { this.modalOpen.set(false); }

  // ----- Image upload (drag & drop / file picker) -----
  onDragOver(ev: DragEvent): void {
    ev.preventDefault();
    this.dragOver.set(true);
  }
  onDragLeave(ev: DragEvent): void {
    ev.preventDefault();
    this.dragOver.set(false);
  }
  onDrop(ev: DragEvent): void {
    ev.preventDefault();
    this.dragOver.set(false);
    const file = ev.dataTransfer?.files?.[0];
    if (file) this.uploadFile(file);
  }
  onFileSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.uploadFile(file);
    input.value = ''; // allow re-selecting the same file
  }
  clearImage(): void {
    this.form.imageUrl = '';
    this.uploadError.set(null);
  }

  private uploadFile(file: File): void {
    this.uploadError.set(null);
    if (!file.type.startsWith('image/')) {
      this.uploadError.set('Please choose an image file.');
      return;
    }
    if (file.size > this.maxImageBytes) {
      this.uploadError.set('Image is too large (max 5MB).');
      return;
    }
    this.uploading.set(true);
    this.admin.uploadProductImage(file).subscribe({
      next: (res) => { this.form.imageUrl = res.url; this.uploading.set(false); },
      error: () => { this.uploadError.set('Upload failed. Check your admin permissions and that the backend is running.'); this.uploading.set(false); },
    });
  }

  private resetUpload(): void {
    this.uploading.set(false);
    this.uploadError.set(null);
    this.dragOver.set(false);
  }

  save(valid: boolean): void {
    this.formError.set(null);
    if (!valid || !this.form.categoryId || !this.form.brandId) {
      this.formError.set('Please fill name, price, category and brand.');
      return;
    }
    this.saving.set(true);
    const editing = this.editing();
    const req$ = editing
      ? this.admin.updateProduct(editing.id, this.form)
      : this.admin.createProduct(this.form);
    req$.subscribe({
      next: () => { this.saving.set(false); this.modalOpen.set(false); this.load(); },
      error: () => { this.saving.set(false); this.formError.set('Save failed. Check your admin permissions.'); },
    });
  }

  remove(p: Product): void {
    if (!confirm(`Delete "${p.name}"?`)) return;
    this.admin.deleteProduct(p.id).subscribe({
      next: () => this.load(),
      error: () => this.error.set('Delete failed.'),
    });
  }

  onImgError(ev: Event): void { (ev.target as HTMLImageElement).src = this.placeholder; }

  private emptyForm(): ProductRequest {
    return { name: '', description: '', price: 0, discount: 0, categoryId: 0, brandId: 0, imageUrl: '' };
  }
}
