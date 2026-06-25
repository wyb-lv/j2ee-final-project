import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Category, Page, Product } from '../models/catalog.models';
import { OrderResponse, OrderStatus } from '../models/checkout.models';
import { Brand, BrandRequest, CategoryRequest, ProductRequest } from './admin.models';

export interface ProductQuery {
  page?: number;
  keyword?: string;
  categoryId?: number;
}

/** All admin/back-office API calls. JWT is attached by the auth interceptor. */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private base = environment.apiBase;

  // ----- Products -----
  listProducts(query: ProductQuery = {}): Observable<Page<Product>> {
    let params = new HttpParams().set('page', String(query.page ?? 1));
    if (query.keyword) params = params.set('keyword', query.keyword);
    if (query.categoryId != null) params = params.set('categoryId', String(query.categoryId));
    return this.http.get<Page<Product>>(`${this.base}/products`, { params });
  }
  createProduct(req: ProductRequest): Observable<Product> {
    return this.http.post<Product>(`${this.base}/products`, req);
  }
  updateProduct(id: number, req: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.base}/products/${id}`, req);
  }
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/products/${id}`);
  }
  /** Uploads an image file (multipart) and returns its stored public URL. */
  uploadProductImage(file: File): Observable<{ url: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ url: string }>(`${this.base}/products/upload`, form);
  }

  // ----- Categories -----
  listCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.base}/categories`);
  }
  createCategory(req: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(`${this.base}/categories`, req);
  }
  updateCategory(id: number, req: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.base}/categories/${id}`, req);
  }
  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/categories/${id}`);
  }

  // ----- Brands -----
  listBrands(): Observable<Brand[]> {
    return this.http.get<Brand[]>(`${this.base}/brands`);
  }
  createBrand(req: BrandRequest): Observable<Brand> {
    return this.http.post<Brand>(`${this.base}/brands`, req);
  }
  updateBrand(id: number, req: BrandRequest): Observable<Brand> {
    return this.http.put<Brand>(`${this.base}/brands/${id}`, req);
  }
  deleteBrand(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/brands/${id}`);
  }

  // ----- Orders -----
  listOrders(excludeStatus?: OrderStatus): Observable<OrderResponse[]> {
    let params = new HttpParams();
    if (excludeStatus) params = params.set('excludeStatus', excludeStatus);
    return this.http.get<OrderResponse[]>(`${this.base}/orders`, { params });
  }
  updateOrderStatus(orderId: number, status: OrderStatus): Observable<OrderResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.put<OrderResponse>(`${this.base}/orders/${orderId}/status`, null, { params });
  }
}
