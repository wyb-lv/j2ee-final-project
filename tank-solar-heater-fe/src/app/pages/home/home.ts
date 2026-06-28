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
    { icon: '⚡', title: 'Tiết kiệm tới 70%', text: 'Cắt giảm hóa đơn năng lượng làm nóng nước với hệ thống hiệu suất cao.' },
    { icon: '☀', title: 'Năng lượng mặt trời', text: 'Tận dụng ánh nắng miễn phí cho nước nóng bất tận, dù mưa hay nắng.' },
    { icon: '🛡', title: 'Bảo hành 10 năm', text: 'Bình chứa và tấm thu thép không gỉ bền vượt trội so với đối thủ.' },
    { icon: '🔧', title: 'Lắp đặt miễn phí', text: 'Kỹ thuật viên được chứng nhận lo việc lắp đặt, đường ống và thu dọn.' },
  ];

  readonly steps = [
    { n: '01', title: 'Chọn hệ thống của bạn', text: 'Bộ thu năng lượng mặt trời, bình chứa, hoặc bơm nhiệt lai — phù hợp với ngôi nhà của bạn.' },
    { n: '02', title: 'Chúng tôi lắp đặt', text: 'Đội ngũ của chúng tôi lắp đặt, đấu đường ống và kiểm tra mọi thứ, thường chỉ trong một ngày.' },
    { n: '03', title: 'Tận hưởng nước nóng', text: 'Điều khiển thông minh giữ nước luôn nóng trong khi âm thầm cắt giảm mức tiêu thụ năng lượng của bạn.' },
  ];

  ngOnInit(): void {
    // Only call the backend in the browser to avoid SSR fetch failures.
    if (!isPlatformBrowser(this.platformId)) return;
    this.loading.set(true);
    this.catalog.getAllProducts({ page: 1 }).subscribe({
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
