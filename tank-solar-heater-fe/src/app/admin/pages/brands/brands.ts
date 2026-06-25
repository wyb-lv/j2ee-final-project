import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../admin.service';
import { Brand } from '../../admin.models';

@Component({
  selector: 'app-admin-brands',
  imports: [CommonModule, FormsModule],
  templateUrl: './brands.html',
  styleUrl: './brands.css',
})
export class AdminBrands implements OnInit {
  private admin = inject(AdminService);

  readonly items = signal<Brand[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly modalOpen = signal(false);
  readonly editing = signal<Brand | null>(null);
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);
  name = '';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.admin.listBrands().subscribe({
      next: (b) => { this.items.set(b); this.loading.set(false); },
      error: () => { this.error.set('Could not load brands.'); this.loading.set(false); },
    });
  }

  openCreate(): void { this.editing.set(null); this.name = ''; this.formError.set(null); this.modalOpen.set(true); }
  openEdit(b: Brand): void { this.editing.set(b); this.name = b.name; this.formError.set(null); this.modalOpen.set(true); }
  closeModal(): void { this.modalOpen.set(false); }

  save(valid: boolean): void {
    if (!valid) { this.formError.set('Name is required.'); return; }
    this.saving.set(true);
    const editing = this.editing();
    const req$ = editing
      ? this.admin.updateBrand(editing.id, { name: this.name })
      : this.admin.createBrand({ name: this.name });
    req$.subscribe({
      next: () => { this.saving.set(false); this.modalOpen.set(false); this.load(); },
      error: () => { this.saving.set(false); this.formError.set('Save failed (admin only).'); },
    });
  }

  remove(b: Brand): void {
    if (!confirm(`Delete brand "${b.name}"?`)) return;
    this.admin.deleteBrand(b.id).subscribe({
      next: () => this.load(),
      error: () => this.error.set('Delete failed — the brand may be in use.'),
    });
  }
}
