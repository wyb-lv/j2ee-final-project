import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: '../auth.css',
})
export class Login {
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  form = { email: '', password: '' };

  get returnUrl(): string {
    return this.route.snapshot.queryParamMap.get('returnUrl') ?? '/';
  }

  get fromCheckout(): boolean {
    return this.returnUrl.startsWith('/checkout');
  }

  submit(valid: boolean): void {
    this.error.set(null);
    if (!valid) {
      this.error.set('Please enter your email and password.');
      return;
    }
    this.submitting.set(true);
    this.auth.login(this.form).subscribe({
      next: () => {
        this.submitting.set(false);
        // Admins go straight to the admin console; everyone else to returnUrl.
        const isAdmin = this.auth.user()?.role?.toLowerCase() === 'admin';
        this.router.navigateByUrl(isAdmin ? '/admin' : this.returnUrl);
      },
      error: (err) => {
        this.submitting.set(false);
        this.error.set(
          err?.status === 0
            ? "Couldn't reach the server. Is the backend running on localhost:8080?"
            : 'Invalid email or password.'
        );
      },
    });
  }
}
