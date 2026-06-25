import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../admin.service';
import { OrderResponse } from '../../../models/checkout.models';

@Component({
  selector: 'app-admin-orders',
  imports: [CommonModule],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class AdminOrders implements OnInit {
  private admin = inject(AdminService);

  readonly orders = signal<OrderResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly expanded = signal<number | null>(null);

  ngOnInit(): void {
    this.admin.listOrders().subscribe({
      next: (o) => { this.orders.set(o); this.loading.set(false); },
      error: () => { this.error.set('Could not load orders. Admin access required.'); this.loading.set(false); },
    });
  }

  toggle(id: number): void {
    this.expanded.update((cur) => (cur === id ? null : id));
  }

  statusColor(status: string): string {
    switch ((status || '').toUpperCase()) {
      case 'PAID':
      case 'COMPLETED':
      case 'DELIVERED': return 'green';
      case 'PENDING': return 'amber';
      case 'SHIPPING':
      case 'PROCESSING': return 'blue';
      case 'CANCELLED': return 'red';
      default: return 'gray';
    }
  }
}
