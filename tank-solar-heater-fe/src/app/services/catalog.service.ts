import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Category, Page, Product } from '../models/catalog.models';

export interface ProductQuery {
  page?: number;        // 1-based (backend expects 1-based)
  keyword?: string;
  categoryId?: number;
  sort?: string;        // e.g. 'price,asc'
}

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private http = inject(HttpClient);
  private base = environment.apiBase;

  getProducts(query: ProductQuery = {}): Observable<Page<Product>> {
    let params = new HttpParams().set('page', String(query.page ?? 1));
    if (query.keyword) params = params.set('keyword', query.keyword);
    if (query.categoryId != null) params = params.set('categoryId', String(query.categoryId));
    if (query.sort) params = params.set('sort', query.sort);
    return this.http.get<Page<Product>>(`${this.base}/products`, { params });
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.base}/products/${id}`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.base}/categories`);
  }
}
