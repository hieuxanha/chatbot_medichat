import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone : true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Khởi tạo form với các điều kiện kiểm tra (Validators)
    this.loginForm = this.fb.group({

      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
      
    });
  }

  // Hàm tiện ích để lấy nhanh các control trong HTML
  get f() { return this.loginForm.controls; }

  onSubmit(): void {
    // Nếu form không hợp lệ thì dừng lại
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    // Giả lập gọi API (Sau này Hiếu sẽ gọi AuthService ở đây)
    console.log('Dữ liệu đăng nhập:', this.loginForm.value);
    
    setTimeout(() => {
      this.isLoading = false;
      // Giả sử đăng nhập thành công, chuyển hướng sang trang Chat
      this.router.navigate(['/chat']);
    }, 1500);
  }
}