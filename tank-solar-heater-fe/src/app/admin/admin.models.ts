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

/** Revenue dashboard figures — all computed server-side. */
export interface TrendPoint {
  date: string;   // YYYY-MM-DD
  total: number;
  pct: number;    // bar height as % of the window's peak
}

export interface DashboardStats {
  dailyRevenue: number;
  monthlyRevenue: number;
  annualRevenue: number;
  trend: TrendPoint[];
}
