import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserProfile } from '../models/user.models';
import { OrderResponse } from '../models/checkout.models';

/** Self-service account calls for the signed-in customer. */
@Injectable({ providedIn: 'root' })
export class AccountService {
  private http = inject(HttpClient);
  private base = environment.apiBase;

  /** The signed-in user's own profile. */
  me(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.base}/users/me`);
  }

  /** Orders placed by the given customer, newest first. */
  myOrders(userId: number): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.base}/orders/${userId}`);
  }
}
