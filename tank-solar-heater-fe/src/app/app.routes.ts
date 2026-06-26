import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { customerGuard } from './auth/customer.guard';
import { adminGuard } from './admin/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home').then((m) => m.Home),
    title: 'SunTank — Solar & Tank Water Heaters',
    canActivate: [customerGuard],
  },
  {
    path: 'about',
    loadComponent: () => import('./pages/about/about').then((m) => m.About),
    title: 'About Us — SunTank',
    canActivate: [customerGuard],
  },
  {
    path: 'shop',
    loadComponent: () => import('./pages/shop/shop').then((m) => m.Shop),
    title: 'Shop Heaters — SunTank',
    canActivate: [customerGuard],
  },
  {
    path: 'product/:id',
    loadComponent: () => import('./pages/product-detail/product-detail').then((m) => m.ProductDetail),
    title: 'Product — SunTank',
    canActivate: [customerGuard],
  },
  {
    path: 'faq',
    loadComponent: () => import('./pages/faq/faq').then((m) => m.Faq),
    title: 'FAQ — SunTank',
    canActivate: [customerGuard],
  },
  {
    path: 'checkout',
    loadComponent: () => import('./pages/checkout/checkout').then((m) => m.Checkout),
    title: 'Checkout — SunTank',
    canActivate: [authGuard, customerGuard],
  },
  {
    path: 'account',
    loadComponent: () => import('./pages/account/account').then((m) => m.Account),
    title: 'My account — SunTank',
    canActivate: [authGuard, customerGuard],
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
    title: 'Log in — SunTank',
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then((m) => m.Register),
    title: 'Create account — SunTank',
  },
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin-layout').then((m) => m.AdminLayout),
    canActivate: [adminGuard],
    children: [
      { path: '', loadComponent: () => import('./admin/pages/dashboard/dashboard').then((m) => m.AdminDashboard), title: 'Admin — SunTank' },
      { path: 'products', loadComponent: () => import('./admin/pages/products/products').then((m) => m.AdminProducts), title: 'Products — Admin' },
      { path: 'categories', loadComponent: () => import('./admin/pages/categories/categories').then((m) => m.AdminCategories), title: 'Categories — Admin' },
      { path: 'brands', loadComponent: () => import('./admin/pages/brands/brands').then((m) => m.AdminBrands), title: 'Brands — Admin' },
      { path: 'orders', loadComponent: () => import('./admin/pages/orders/orders').then((m) => m.AdminOrders), title: 'Orders — Admin' },
    ],
  },
  { path: '**', redirectTo: '' },
];
