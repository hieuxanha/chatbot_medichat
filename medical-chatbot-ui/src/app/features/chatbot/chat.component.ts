import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type Role = 'assistant' | 'user';

interface Message {
  id: number;
  role: Role;
  text: string;
  time: string;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent {
  @ViewChild('scrollWrap') scrollWrap!: ElementRef<HTMLDivElement>;

  messageText = '';
  messages: Message[] = [
    { id: 1, role: 'assistant', text: "Hello John! I'm here to help you analyze your symptoms. How are you feeling today? Please describe any discomfort or health concerns you've noticed.", time: '10:24 AM' },
    { id: 2, role: 'user', text: "I've had a persistent headache behind my eyes and some mild nausea since I woke up this morning.", time: '10:25 AM' },
    { id: 3, role: 'assistant', text: `Based on your symptoms (headache behind eyes and nausea), it could be several things. Let's look at the most likely possibilities:`, time: '10:25 AM' }
  ];

  private nextId = 4;

  sendMessage() {
    const text = this.messageText.trim();
    if (!text) return;
    const now = new Date();
    const time = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    this.messages.push({ id: this.nextId++, role: 'user', text, time });
    this.messageText = '';
    this.scrollToBottom();

    // Simulate assistant reply
    setTimeout(() => {
      this.messages.push({ id: this.nextId++, role: 'assistant', text: 'Thank you — I see. Can you tell me when the pain started and any known triggers?', time });
      this.scrollToBottom();
    }, 800);
  }

  private scrollToBottom() {
    setTimeout(() => {
      try {
        const el = this.scrollWrap?.nativeElement;
        if (el) el.scrollTop = el.scrollHeight;
      } catch (e) {}
    }, 50);
  }
}
