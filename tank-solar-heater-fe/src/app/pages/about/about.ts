import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-about',
  imports: [CommonModule, RouterLink],
  templateUrl: './about.html',
  styleUrl: './about.css',
})
export class About {
  readonly stats = [
    { value: '2009', label: 'Sưởi ấm các gia đình từ năm' },
    { value: '12,000+', label: 'Hệ thống đã lắp đặt' },
    { value: '70%', label: 'Năng lượng tiết kiệm trung bình' },
    { value: '4.9★', label: 'Đánh giá của khách hàng' },
  ];

  readonly values = [
    { icon: '☀', title: 'Ưu tiên năng lượng sạch', text: 'Mỗi hệ thống chúng tôi bán đều được thiết kế để tạo ra nhiều nước nóng nhất với mức năng lượng thấp nhất — ánh nắng trước, điện sau.' },
    { icon: '🛡', title: 'Bền bỉ theo thời gian', text: 'Bình chứa thép không gỉ, tấm thu chống ăn mòn và linh kiện mà chúng tôi sẵn lòng lắp cho chính ngôi nhà của mình.' },
    { icon: '🤝', title: 'Tư vấn trung thực', text: 'Chúng tôi chọn hệ thống phù hợp với ngôi nhà và khí hậu của bạn — không phải loại đắt nhất trên kệ.' },
    { icon: '🔧', title: 'Hỗ trợ tận tâm', text: 'Kỹ thuật viên được chứng nhận cho việc lắp đặt, bảo trì và bảo hành — lâu dài sau khi bán.' },
  ];
}
