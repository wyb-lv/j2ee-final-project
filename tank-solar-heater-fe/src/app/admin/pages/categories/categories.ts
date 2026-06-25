import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../admin.service';
import { Category } from '../../../models/catalog.models';

@Component({
  selector: 'app-admin-categories',
  imports: [CommonModule, FormsModule],
  templateUrl: './categories.html',
  styleUrl: './categories.css',
})
export class AdminCategories implements OnInit {
  private admin = inject(AdminService);

  readonly items = signal<Category[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly modalOpen = signal(false);
  readonly editing = signal<Category | null>(null);
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);
  name = '';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.admin.listCategories().subscribe({
      next: (c) => { this.items.set(c); this.loading.set(false); },
      error: () => { this.error.set('Could not load categories.'); this.loading.set(false); },
    });
  }

  openCreate(): void { this.editing.set(null); this.name = ''; this.formError.set(null); this.modalOpen.set(true); }
  openEdit(c: Category): void { this.editing.set(c); this.name = c.name; this.formError.set(null); this.modalOpen.set(true); }
  closeModal(): void { this.modalOpen.set(false); }

  save(valid: boolean): void {
    if (!valid) { this.formError.set('Name is required.'); return; }
    this.saving.set(true);
    const editing = this.editing();
    const req$ = editing
      ? this.admin.updateCategory(editing.id, { name: this.name })
      : this.admin.createCategory({ name: this.name });
    req$.subscribe({
      next: () => { this.saving.set(false); this.modalOpen.set(false); this.load(); },
      error: () => { this.saving.set(false); this.formError.set('Save failed (admin only).'); },
    });
  }

  remove(c: Category): void {
    if (!confirm(`Delete category "${c.name}"?`)) return;
    this.admin.deleteCategory(c.id).subscribe({
      next: () => this.load(),
      error: () => this.error.set('Delete failed — the category may be in use.'),
    });
  }
}
