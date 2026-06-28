import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { customerGuard } from './auth/customer.guard';
import { adminGuard } from './admin/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home').then((m) => m.Home),
    title: 'SunTank — Máy nước nóng năng lượng mặt trời & bình chứa',
    canActivate: [customerGuard],
  },
  {
    path: 'about',
    loadComponent: () => import('./pages/about/about').then((m) => m.About),
    title: 'Giới thiệu — SunTank',
    canActivate: [customerGuard],
  },
  {
    path: 'shop',
    loadComponent: () => import('./pages/shop/shop').then((m) => m.Shop),
    title: 'Cửa hàng — SunTank',
    canActivate: [customerGuard],
  },
  {
    path: 'product/:id',
    loadComponent: () => import('./pages/product-detail/product-detail').then((m) => m.ProductDetail),
    title: 'Sản phẩm — SunTank',
    canActivate: [customerGuard],
  },
  {
    path: 'faq',
    loadComponent: () => import('./pages/faq/faq').then((m) => m.Faq),
    title: 'Hỏi đáp — SunTank',
    canActivate: [customerGuard],
  },
  {
    path: 'checkout',
    loadComponent: () => import('./pages/checkout/checkout').then((m) => m.Checkout),
    title: 'Thanh toán — SunTank',
    canActivate: [authGuard, customerGuard],
  },
  {
    path: 'account',
    loadComponent: () => import('./pages/account/account').then((m) => m.Account),
    title: 'Tài khoản — SunTank',
    canActivate: [authGuard, customerGuard],
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
    title: 'Đăng nhập — SunTank',
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then((m) => m.Register),
    title: 'Tạo tài khoản — SunTank',
  },
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin-layout').then((m) => m.AdminLayout),
    canActivate: [adminGuard],
    children: [
      { path: '', loadComponent: () => import('./admin/pages/dashboard/dashboard').then((m) => m.AdminDashboard), title: 'Quản trị — SunTank' },
      { path: 'products', loadComponent: () => import('./admin/pages/products/products').then((m) => m.AdminProducts), title: 'Sản phẩm — Quản trị' },
      { path: 'categories', loadComponent: () => import('./admin/pages/categories/categories').then((m) => m.AdminCategories), title: 'Danh mục — Quản trị' },
      { path: 'brands', loadComponent: () => import('./admin/pages/brands/brands').then((m) => m.AdminBrands), title: 'Thương hiệu — Quản trị' },
      { path: 'orders', loadComponent: () => import('./admin/pages/orders/orders').then((m) => m.AdminOrders), title: 'Đơn hàng — Quản trị' },
    ],
  },
  { path: '**', redirectTo: '' },
];
