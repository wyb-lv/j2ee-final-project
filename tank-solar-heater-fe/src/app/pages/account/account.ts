import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AccountService } from '../../services/account.service';
import { AuthService } from '../../auth/auth.service';
import { UserProfile } from '../../models/user.models';
import { OrderResponse } from '../../models/checkout.models';

@Component({
  selector: 'app-account',
  imports: [CommonModule, RouterLink],
  templateUrl: './account.html',
  styleUrl: './account.css',
})
export class Account implements OnInit {
  private accountService = inject(AccountService);
  private auth = inject(AuthService);

  readonly profile = signal<UserProfile | null>(null);
  readonly orders = signal<OrderResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly expanded = signal<number | null>(null);

  readonly orderCount = computed(() => this.orders().length);

  ngOnInit(): void {
    const userId = this.auth.user()?.id;
    if (!userId) {
      this.error.set('Bạn cần đăng nhập để xem tài khoản.');
      this.loading.set(false);
      return;
    }

    forkJoin({
      profile: this.accountService.me(),
      orders: this.accountService.myOrders(userId),
    }).subscribe({
      next: ({ profile, orders }) => {
        this.profile.set(profile);
        // Newest first.
        this.orders.set([...orders].sort((a, b) => b.id - a.id));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Không thể tải tài khoản. Vui lòng thử lại.');
        this.loading.set(false);
      },
    });
  }

  toggle(id: number): void {
    this.expanded.update((cur) => (cur === id ? null : id));
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

  paymentColor(status: string | null): string {
    switch ((status || '').toUpperCase()) {
      case 'PAID': return 'green';
      case 'PENDING': return 'amber';
      case 'FAILED': return 'red';
      case 'REFUNDED': return 'blue';
      default: return 'gray';
    }
  }

  // ----- Vietnamese display labels for backend enum values -----

  statusLabel(status: string): string {
    switch ((status || '').toUpperCase()) {
      case 'PENDING': return 'Chờ xử lý';
      case 'SHIPPING': return 'Đang giao';
      case 'DONE': return 'Hoàn thành';
      case 'CANCELLED': return 'Đã hủy';
      default: return status;
    }
  }

  paymentLabel(status: string | null): string {
    switch ((status || '').toUpperCase()) {
      case 'PAID': return 'Đã thanh toán';
      case 'PENDING': return 'Chờ thanh toán';
      case 'FAILED': return 'Thất bại';
      case 'REFUNDED': return 'Đã hoàn tiền';
      default: return status ?? '';
    }
  }

  methodLabel(method: string | null): string {
    switch ((method || '').toUpperCase()) {
      case 'COD': return 'Thanh toán khi nhận hàng';
      case 'BANK_TRANSFER': return 'Chuyển khoản ngân hàng';
      default: return method ?? '';
    }
  }

  roleLabel(role: string | null): string {
    switch ((role || '').toUpperCase()) {
      case 'ADMIN': return 'Quản trị viên';
      case 'CUSTOMER': return 'Khách hàng';
      default: return role ?? '';
    }
  }
}
