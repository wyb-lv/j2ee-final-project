import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home').then((m) => m.Home),
    title: 'SunTank — Solar & Tank Water Heaters',
  },
  {
    path: 'about',
    loadComponent: () => import('./pages/about/about').then((m) => m.About),
    title: 'About Us — SunTank',
  },
  {
    path: 'shop',
    loadComponent: () => import('./pages/shop/shop').then((m) => m.Shop),
    title: 'Shop Heaters — SunTank',
  },
  {
    path: 'faq',
    loadComponent: () => import('./pages/faq/faq').then((m) => m.Faq),
    title: 'FAQ — SunTank',
  },
  {
    path: 'checkout',
    loadComponent: () => import('./pages/checkout/checkout').then((m) => m.Checkout),
    title: 'Checkout — SunTank',
  },
  { path: '**', redirectTo: '' },
];
