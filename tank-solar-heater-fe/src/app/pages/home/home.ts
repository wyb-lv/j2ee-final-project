import { Component, OnInit, PLATFORM_ID, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CatalogService } from '../../services/catalog.service';
import { Product } from '../../models/catalog.models';
import { ProductCard } from '../../components/product-card/product-card';

@Component({
  selector: 'app-home',
  imports: [CommonModule, RouterLink, ProductCard],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  private catalog = inject(CatalogService);
  private platformId = inject(PLATFORM_ID);

  readonly featured = signal<Product[]>([]);
  readonly loading = signal(false);
  readonly failed = signal(false);

  readonly features = [
    { icon: '⚡', title: 'Up to 70% Savings', text: 'Cut your water-heating energy bill with high-efficiency systems.' },
    { icon: '☀', title: 'Solar Powered', text: 'Harness free sunshine for endless hot water, rain or shine.' },
    { icon: '🛡', title: '10-Year Warranty', text: 'Stainless tanks and panels built to outlast the competition.' },
    { icon: '🔧', title: 'Free Installation', text: 'Certified technicians handle setup, plumbing and disposal.' },
  ];

  readonly steps = [
    { n: '01', title: 'Pick your system', text: 'Solar collector, storage tank, or hybrid heat pump — sized to your home.' },
    { n: '02', title: 'We install it', text: 'Our team mounts, plumbs and tests everything, usually in a single day.' },
    { n: '03', title: 'Enjoy hot water', text: 'Smart controls keep water hot while quietly slashing your energy use.' },
  ];

  ngOnInit(): void {
    // Only call the backend in the browser to avoid SSR fetch failures.
    if (!isPlatformBrowser(this.platformId)) return;
    this.loading.set(true);
    this.catalog.getProducts({ page: 1 }).subscribe({
      next: (page) => {
        this.featured.set(page.content.slice(0, 4));
        this.loading.set(false);
      },
      error: () => {
        this.failed.set(true);
        this.loading.set(false);
      },
    });
  }
}
