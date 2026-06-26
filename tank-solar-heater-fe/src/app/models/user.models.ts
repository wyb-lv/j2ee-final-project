// Mirrors UserResponse in tank-solar-heater-be.
export interface UserProfile {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  role: string;
  enabled: boolean;
}
