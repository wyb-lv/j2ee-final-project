import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/** Talks to the admin assistant chatbot. JWT is attached by the auth interceptor. */
@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private base = environment.apiBase;

  /** Sends a message and returns the assistant's plain-text reply. */
  send(message: string): Observable<string> {
    return this.http.post(`${this.base}/chat/`, { message }, { responseType: 'text' });
  }
}
