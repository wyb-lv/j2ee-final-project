// Mirrors the Spring Boot DTOs returned by tank-solar-heater-be.

export interface Category {
  id: number;
  name: string;
}

export interface Brand {
  id: number;
  name: string;
}

export interface Product {
  id: number;
  categoryId: number;
  categoryName: string;
  brandId: number;
  brandName: string;
  name: string;
  description: string;
  price: number;
  discount: number; // percentage, e.g. 10 = 10% off
  imageUrl: string;
}

// Spring Data Page<T> envelope.
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // 0-based page index
  size: number;
  first: boolean;
  last: boolean;
}
