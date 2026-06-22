// Mirrors the backend cart module DTOs (POST /api/cart).

export interface CartLine {
  productId: number;
  name: string;
  imageUrl: string;
  price: number;
  discount: number;
  finalPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface CartPricing {
  lines: CartLine[];
  itemCount: number;
  subtotal: number;
  discountTotal: number;
  total: number;
}
