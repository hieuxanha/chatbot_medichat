import { Component, ElementRef, ViewChild,PLATFORM_ID, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http'; // THÊM DÒNG NÀY
import { isPlatformBrowser } from '@angular/common';
type Role = 'assistant' | 'user';
import { MarkdownComponent } from 'ngx-markdown'; // Import component này

interface Message {
  id: number;
  role: Role;
  text: string;
  time: string;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule,MarkdownComponent],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {
  @ViewChild('scrollWrap') scrollWrap!: ElementRef<HTMLDivElement>;

  messageText = '';
  messages: Message[] = [];
  private nextId = 1;
  userName = 'John Doe'; // Hiếu có thể lấy từ localStorage.getItem('user_name')

  constructor(private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {} // KHAI BÁO HTTP TẠI ĐÂY

  ngOnInit(): void {
    // Hiếu có thể lấy tên user thật đã lưu lúc Login
    this.userName = localStorage.getItem('user_name') || 'Người dùng';
    

    if (isPlatformBrowser(this.platformId)) {
      this.userName = localStorage.getItem('user_name') || 'Người dùng';
    }

    // Tin nhắn chào mừng mặc định
    this.messages.push({
      id: this.nextId++,
      role: 'assistant',
      text: `Xin chào ${this.userName}! Tôi là trợ lý MediAI. Bạn đang cảm thấy thế nào? Hãy mô tả triệu chứng của bạn nhé.`,
      time: this.getCurrentTime()
    });
  }

  // Hàm gọi API thật đến Backend Spring Boot
  sendMessage() {
    const text = this.messageText.trim();
    if (!text) return;

    const time = this.getCurrentTime();

    // 1. Hiển thị tin nhắn của User
    this.messages.push({ id: this.nextId++, role: 'user', text, time });
    this.messageText = '';
    this.scrollToBottom();

    // 2. Gọi API Backend (Endpoint /api/chat/ask như mình đã viết ở Backend)
    // Lưu ý: Đổi URL nếu Hiếu đặt tên endpoint khác
    this.http.post<any>('http://localhost:8080/api/chat/ask', { message: text }).subscribe({
      next: (res) => {
        this.messages.push({ 
          id: this.nextId++, 
          role: 'assistant', 
          text: res.text, 
          time: res.time || this.getCurrentTime() 
        });
        this.scrollToBottom();
      },
      error: (err) => {
        console.error('Lỗi kết nối AI:', err);
        this.messages.push({ 
          id: this.nextId++, 
          role: 'assistant', 
          text: 'Rất tiếc, kết nối với AI bị gián đoạn. Hiếu kiểm tra Backend đã chạy chưa nhé!', 
          time: this.getCurrentTime() 
        });
        this.scrollToBottom();
      }
    });
  }

  private getCurrentTime(): string {
    return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  private scrollToBottom() {
    setTimeout(() => {
      try {
        const el = this.scrollWrap?.nativeElement;
        if (el) el.scrollTop = el.scrollHeight;
      } catch (e) {}
    }, 100);
  }
}