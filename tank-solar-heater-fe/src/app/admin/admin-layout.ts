import { Component, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { AdminChat } from './components/admin-chat/admin-chat';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, AdminChat],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout {
  protected readonly auth = inject(AuthService);
  private router = inject(Router);
  protected readonly sidebarOpen = signal(false);

  readonly nav = [
    { path: '/admin', exact: true, icon: 'dashboard', label: 'Dashboard' },
    { path: '/admin/products', exact: false, icon: 'water_drop', label: 'Products' },
    { path: '/admin/categories', exact: false, icon: 'category', label: 'Categories' },
    { path: '/admin/brands', exact: false, icon: 'verified', label: 'Brands' },
    { path: '/admin/orders', exact: false, icon: 'receipt_long', label: 'Orders' },
  ];

  toggleSidebar() {
    this.sidebarOpen.update((v) => !v);
  }
  closeSidebar() {
    this.sidebarOpen.set(false);
  }
  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }
}
