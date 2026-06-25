export interface ProductRequest {
  name: string;
  description: string;
  price: number;
  discount: number;
  categoryId: number;
  brandId: number;
  imageUrl: string;
}

export interface Brand {
  id: number;
  name: string;
}

export interface BrandRequest {
  name: string;
}

export interface CategoryRequest {
  name: string;
}
