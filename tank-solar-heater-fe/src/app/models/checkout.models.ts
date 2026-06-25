// Mirrors the checkout/payment DTOs in tank-solar-heater-be.

export interface CheckoutItem {
  productId: number;
  quantity: number;
}

export interface CheckoutRequest {
  customerName: string;
  customerEmail: string;
  customerPhone?: string;
  customerAddress?: string;
  paymentMethod: string; // 'COD' | 'BANK_TRANSFER' | 'CARD'
  items: CheckoutItem[];
}

export interface PaymentResponse {
  id: number;
  orderId: number;
  paymentMethod: string;
  paymentStatus: string;
  amount: number;
  transactionId: string | null;
  createdAt: string;
  paidAt: string | null;
}

export interface OrderItemResponse {
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  discount: number;
}

export interface OrderResponse {
  id: number;
  customerId: number;
  date: string;
  status: string;
  address: string | null;
  employeeId: number | null;
  total: number;
  items: OrderItemResponse[];
}

export interface CheckoutResponse {
  order: OrderResponse;
  payment: PaymentResponse;
}
