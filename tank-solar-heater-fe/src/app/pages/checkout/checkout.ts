import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { CheckoutService } from '../../services/checkout.service';
import { CheckoutResponse } from '../../models/checkout.models';

@Component({
  selector: 'app-checkout',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout implements OnInit {
  protected readonly cart = inject(CartService);
  private checkoutService = inject(CheckoutService);

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
    // Re-price the cart server-side as soon as the checkout page loads.
    this.cart.revalidate();
  }

  placeOrder(formValid: boolean): void {
    this.error.set(null);
    if (!formValid) {
      this.error.set('Please fill in your name, a valid email and an address.');
      return;
    }
    if (this.cart.items().length === 0) return;

    this.submitting.set(true);
    this.checkoutService
      .checkout({
        ...this.form,
        items: this.cart.items().map((i) => ({ productId: i.product.id, quantity: i.qty })),
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
