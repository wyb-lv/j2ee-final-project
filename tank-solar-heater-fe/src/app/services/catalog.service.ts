import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Brand, Category, Page, Product } from '../models/catalog.models';

/** Page + sort applied on top of whichever single filter endpoint is used. */
export interface PageQuery {
  page?: number;   // 1-based (backend expects 1-based)
  sort?: string;   // e.g. 'price,asc'
}

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private http = inject(HttpClient);
  private base = environment.apiBase;

  /** Each filter has its own endpoint; only one is ever active at a time. */

  getAllProducts(q: PageQuery = {}): Observable<Page<Product>> {
    return this.http.get<Page<Product>>(`${this.base}/products`, { params: this.pageParams(q) });
  }

  searchProducts(keyword: string, q: PageQuery = {}): Observable<Page<Product>> {
    const params = this.pageParams(q).set('keyword', keyword);
    return this.http.get<Page<Product>>(`${this.base}/products/search`, { params });
  }

  getProductsByCategory(categoryId: number, q: PageQuery = {}): Observable<Page<Product>> {
    const params = this.pageParams(q).set('categoryId', String(categoryId));
    return this.http.get<Page<Product>>(`${this.base}/products/by-category`, { params });
  }

  getProductsByBrand(brandId: number, q: PageQuery = {}): Observable<Page<Product>> {
    const params = this.pageParams(q).set('brandId', String(brandId));
    return this.http.get<Page<Product>>(`${this.base}/products/by-brand`, { params });
  }

  getProductsByPrice(minPrice: number | null, maxPrice: number | null, q: PageQuery = {}): Observable<Page<Product>> {
    let params = this.pageParams(q);
    if (minPrice != null) params = params.set('minPrice', String(minPrice));
    if (maxPrice != null) params = params.set('maxPrice', String(maxPrice));
    return this.http.get<Page<Product>>(`${this.base}/products/by-price`, { params });
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.base}/products/${id}`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.base}/categories`);
  }

  getBrands(): Observable<Brand[]> {
    return this.http.get<Brand[]>(`${this.base}/brands`);
  }

  private pageParams(q: PageQuery): HttpParams {
    let params = new HttpParams().set('page', String(q.page ?? 1));
    if (q.sort) params = params.set('sort', q.sort);
    return params;
  }
}
