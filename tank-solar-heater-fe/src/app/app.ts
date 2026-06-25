import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';
import { CartService } from './services/cart.service';
import { AuthService } from './auth/auth.service';
import { resolveImageUrl } from './shared/image.util';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly cart = inject(CartService);
  protected readonly auth = inject(AuthService);
  protected readonly resolveImageUrl = resolveImageUrl;
  private router = inject(Router);
  protected readonly year = new Date().getFullYear();
  protected readonly menuOpen = signal(false);
  // Hide the storefront header/footer/cart on admin routes.
  protected readonly chromeless = signal(false);

  constructor() {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => this.chromeless.set(e.urlAfterRedirects.startsWith('/admin')));
  }
  protected readonly userMenuOpen = signal(false);

  toggleMenu() {
    this.menuOpen.update((v) => !v);
  }
  closeMenu() {
    this.menuOpen.set(false);
  }
  toggleUserMenu() {
    this.userMenuOpen.update((v) => !v);
  }
  closeUserMenu() {
    this.userMenuOpen.set(false);
  }
  logout() {
    this.auth.logout();
    this.closeUserMenu();
  }
}
