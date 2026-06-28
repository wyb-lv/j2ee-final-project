import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../admin.service';
import { OrderResponse, OrderStatus } from '../../../models/checkout.models';

@Component({
  selector: 'app-admin-orders',
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
})
export class AdminOrders implements OnInit {
  private admin = inject(AdminService);

  readonly orders = signal<OrderResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly expanded = signal<number | null>(null);

  readonly statuses: OrderStatus[] = ['PENDING', 'SHIPPING', 'DONE', 'CANCELLED'];
  readonly paymentStatuses: string[] = ['PENDING', 'PAID', 'FAILED', 'REFUNDED'];

  ngOnInit(): void {
    this.admin.listOrders().subscribe({
      next: (o) => { this.orders.set(o); this.loading.set(false); },
      error: () => { this.error.set('Could not load orders. Admin access required.'); this.loading.set(false); },
    });
  }

  toggle(id: number): void {
    this.expanded.update((cur) => (cur === id ? null : id));
  }

  onStatusChange(order: OrderResponse, newStatus: OrderStatus): void {
    this.admin.updateOrderStatus(order.id, newStatus).subscribe({
      next: (updated) => {
        this.orders.update((list) =>
          list.map((o) => (o.id === updated.id ? updated : o))
        );
      },
      error: () => {
        this.error.set(`Failed to update status for order #${order.id}.`);
      },
    });
  }

  onPaymentStatusChange(order: OrderResponse, newStatus: string): void {
    if (order.paymentId == null) return;
    this.admin.updatePaymentStatus(order.paymentId, newStatus).subscribe({
      next: (payment) => {
        this.orders.update((list) =>
          list.map((o) =>
            o.id === order.id
              ? { ...o, paymentStatus: payment.paymentStatus, paidAt: payment.paidAt }
              : o
          )
        );
      },
      error: () => {
        this.error.set(`Failed to update payment status for order #${order.id}.`);
      },
    });
  }

  statusColor(status: string): string {
    switch ((status || '').toUpperCase()) {
      case 'DONE': return 'green';
      case 'PENDING': return 'amber';
      case 'SHIPPING': return 'blue';
      case 'CANCELLED': return 'red';
      default: return 'gray';
    }
  }

  payColor(status: string | null): string {
    switch ((status || '').toUpperCase()) {
      case 'PAID': return 'green';
      case 'PENDING': return 'amber';
      case 'FAILED': return 'red';
      case 'REFUNDED': return 'blue';
      default: return 'gray';
    }
  }
}
