import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-register',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: '../auth.css',
})
export class Register {
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  form = { name: '', email: '', password: '', phone: '' };

  get returnUrl(): string {
    return this.route.snapshot.queryParamMap.get('returnUrl') ?? '/';
  }

  submit(valid: boolean): void {
    this.error.set(null);
    if (!valid) {
      this.error.set('Vui lòng nhập họ tên, email hợp lệ và mật khẩu.');
      return;
    }
    this.submitting.set(true);
    this.auth.register(this.form).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (err) => {
        this.submitting.set(false);
        this.error.set(
          err?.status === 0
            ? 'Không thể kết nối đến máy chủ. Backend có đang chạy trên localhost:8080 không?'
            : 'Không thể tạo tài khoản. Email có thể đã được sử dụng.'
        );
      },
    });
  }
}
