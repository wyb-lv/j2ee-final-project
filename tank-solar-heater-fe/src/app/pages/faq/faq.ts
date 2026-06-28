import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface FaqItem {
  q: string;
  a: string;
  group: string;
}

@Component({
  selector: 'app-faq',
  imports: [CommonModule, RouterLink],
  templateUrl: './faq.html',
  styleUrl: './faq.css',
})
export class Faq {
  readonly open = signal<number | null>(0);

  toggle(i: number): void {
    this.open.update((cur) => (cur === i ? null : i));
  }

  readonly items: FaqItem[] = [
    {
      group: 'Máy nước nóng năng lượng mặt trời',
      q: 'Máy nước nóng năng lượng mặt trời hoạt động như thế nào?',
      a: 'Bộ thu lắp trên mái nhà hấp thụ ánh nắng và làm nóng một chất lỏng truyền nhiệt cho nước trong bồn chứa của bạn. Vào những ngày nắng, mặt trời đảm nhận hầu hết công việc; vào những ngày nhiều mây, bộ phận gia nhiệt dự phòng hoặc bơm nhiệt sẽ bổ sung để bạn không bao giờ bị thiếu nước nóng.',
    },
    {
      group: 'Máy nước nóng năng lượng mặt trời',
      q: 'Máy nước nóng năng lượng mặt trời có hoạt động vào mùa đông hay những ngày nhiều mây không?',
      a: 'Có. Bộ thu ống chân không của chúng tôi vẫn thu được nhiệt trong điều kiện ánh sáng yếu và nhiệt độ đóng băng, và mọi hệ thống năng lượng mặt trời đều đi kèm bộ phận dự phòng bằng điện hoặc bơm nhiệt để giữ nước luôn nóng khi ánh nắng không đủ.',
    },
    {
      group: 'Máy nước nóng bồn chứa',
      q: 'Tôi cần bồn chứa dung tích bao nhiêu?',
      a: 'Theo nguyên tắc chung: 100–150L cho 1–2 người, 200–300L cho gia đình 3–4 người, và 400L trở lên cho hộ gia đình lớn hơn. Đội ngũ của chúng tôi sẽ tính toán dung tích bồn phù hợp với nhu cầu sử dụng của bạn trong buổi tư vấn miễn phí, để bạn không bao giờ thiếu nước — hay phải trả tiền làm nóng lượng nước không dùng đến.',
    },
    {
      group: 'Máy nước nóng bồn chứa',
      q: 'Nước nóng giữ được trong bao lâu?',
      a: 'Bồn inox cách nhiệt giữ nhiệt trong nhiều giờ, vì vậy một bồn đầy đủ thoải mái cho nhiều người tắm liên tiếp. Bộ điều khiển thông minh cũng làm nóng lại theo lịch để bồn luôn đầy khi bạn cần nhất.',
    },
    {
      group: 'Tiết kiệm',
      q: 'Tôi có thể thực sự tiết kiệm được bao nhiêu?',
      a: 'Đun nước nóng thường chiếm 15–25% hóa đơn năng lượng của một ngôi nhà. Khách hàng chuyển từ máy nước nóng điện thông thường sang hệ thống năng lượng mặt trời hoặc bơm nhiệt tiết kiệm tới 70% chi phí đun nước nóng — hầu hết các hệ thống tự hoàn vốn trong vài năm.',
    },
    {
      group: 'Lắp đặt',
      q: 'Các bạn có lo việc lắp đặt không?',
      a: 'Chắc chắn rồi. Kỹ thuật viên được chứng nhận đảm nhận việc lắp đặt, đường ống, điện và tháo dỡ thiết bị cũ của bạn — thường trong một ngày. Lắp đặt được bao gồm cho hầu hết các hệ thống.',
    },
    {
      group: 'Lắp đặt',
      q: 'Tôi có thể chuyển từ máy nước nóng điện hoặc gas cũ không?',
      a: 'Trong hầu hết mọi trường hợp, có. Chúng tôi đánh giá thiết lập hiện tại của bạn và đề xuất hệ thống năng lượng mặt trời, bồn chứa hoặc lai phù hợp với đường ống và khí hậu của bạn, sau đó xử lý toàn bộ việc chuyển đổi cho bạn.',
    },
    {
      group: 'Bảo hành',
      q: 'Máy nước nóng của các bạn được bảo hành như thế nào?',
      a: 'Bồn chứa và bộ thu năng lượng mặt trời được bảo hành 10 năm, với 2–5 năm cho các linh kiện điện tử tùy theo mẫu máy. Mỗi lần lắp đặt đều bao gồm dịch vụ bảo trì và hỗ trợ bảo hành liên tục từ chính kỹ thuật viên của chúng tôi.',
    },
    {
      group: 'Đơn hàng',
      q: 'Việc giao hàng và thanh toán hoạt động như thế nào?',
      a: 'Thêm một hệ thống vào giỏ hàng và thanh toán trực tuyến. Chúng tôi xác nhận dung tích, lên lịch giao hàng và lắp đặt, đồng thời cung cấp các tùy chọn thanh toán linh hoạt. Giá hiển thị đã bao gồm mọi ưu đãi đang áp dụng.',
    },
  ];
}
