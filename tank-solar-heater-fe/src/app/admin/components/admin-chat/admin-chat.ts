import { Component, ElementRef, inject, signal, viewChild, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../chat.service';

interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
}

@Component({
  selector: 'app-admin-chat',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-chat.html',
  styleUrl: './admin-chat.css',
})
export class AdminChat {
  private chat = inject(ChatService);

  private readonly scrollBox = viewChild<ElementRef<HTMLDivElement>>('scrollBox');

  readonly open = signal(false);
  readonly sending = signal(false);
  readonly draft = signal('');
  readonly messages = signal<ChatMessage[]>([]);

  constructor() {
    // Keep the conversation scrolled to the latest message.
    effect(() => {
      this.messages();
      const box = this.scrollBox()?.nativeElement;
      if (box) queueMicrotask(() => (box.scrollTop = box.scrollHeight));
    });
  }

  toggle(): void {
    this.open.update((v) => !v);
  }

  send(): void {
    const text = this.draft().trim();
    if (!text || this.sending()) return;

    this.messages.update((m) => [...m, { role: 'user', text }]);
    this.draft.set('');
    this.sending.set(true);

    this.chat.send(text).subscribe({
      next: (reply) => {
        this.messages.update((m) => [...m, { role: 'assistant', text: reply }]);
        this.sending.set(false);
      },
      error: () => {
        this.messages.update((m) => [
          ...m,
          { role: 'assistant', text: 'Xin lỗi, không thể kết nối tới trợ lý lúc này. Vui lòng thử lại.' },
        ]);
        this.sending.set(false);
      },
    });
  }
}
