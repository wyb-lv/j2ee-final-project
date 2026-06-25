import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { CheckoutService } from '../../services/checkout.service';
import { AuthService } from '../../auth/auth.service';
import { CheckoutResponse } from '../../models/checkout.models';
import { resolveImageUrl } from '../../shared/image.util';

@Component({
  selector: 'app-checkout',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout implements OnInit {
  protected readonly cart = inject(CartService);
  private checkoutService = inject(CheckoutService);
  private auth = inject(AuthService);
  protected readonly resolveImageUrl = resolveImageUrl;

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<CheckoutResponse | null>(null);

  readonly paymentMethods = [
    { value: 'COD', label: 'Cash on delivery', hint: 'Pay when your heater arrives.' },
    { value: 'BANK_TRANSFER', label: 'Bank transfer', hint: 'We email you transfer details.' },
    { value: 'CARD', label: 'Credit / debit card', hint: 'Pay securely after we confirm.' },
  ];

  // form model
  form = {
    customerName: '',
    customerEmail: '',
    customerPhone: '',
    customerAddress: '',
    paymentMethod: 'COD',
  };

  ngOnInit(): void {
    // Load the latest session cart from the server when the page opens.
    this.cart.load();

    // Prefill from the signed-in account (checkout is gated behind login).
    const user = this.auth.user();
    if (user) {
      this.form.customerName = user.name;
      this.form.customerEmail = user.email;
    }
  }

  placeOrder(formValid: boolean): void {
    this.error.set(null);
    if (!formValid) {
      this.error.set('Please fill in your name, a valid email and an address.');
      return;
    }
    if (this.cart.lines().length === 0) return;

    this.submitting.set(true);
    this.checkoutService
      .checkout({
        ...this.form,
        items: this.cart.lines().map((l) => ({ productId: l.productId, quantity: l.quantity })),
      })
      .subscribe({
        next: (res) => {
          this.result.set(res);
          this.submitting.set(false);
          this.cart.clear();
        },
        error: (err) => {
          this.submitting.set(false);
          this.error.set(
            err?.status === 0
              ? "Couldn't reach the server. Is the backend running on localhost:8080?"
              : 'Something went wrong placing your order. Please try again.'
          );
        },
      });
  }
}
