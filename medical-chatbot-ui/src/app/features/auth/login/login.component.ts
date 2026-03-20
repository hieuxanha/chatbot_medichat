import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

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
    private router: Router,
    private authService: AuthService
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

    const credentials = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password
    };

   // Gọi API Đăng nhập thật từ Backend
    this.authService.login(credentials).subscribe({
      next: (response) => {
        this.isLoading = false;
        
        // 1. Lưu Token và thông tin User vào localStorage
        localStorage.setItem('token', response.token);
        localStorage.setItem('user_email', response.email);
        localStorage.setItem('user_name', response.fullName);

        console.log('Đăng nhập thành công, nhận Token:', response.token);

        // 2. Chuyển hướng sang trang Chat
        this.router.navigate(['/chat']);
      },
      error: (err) => {
        this.isLoading = false;
        // Hiển thị thông báo lỗi từ Backend (ví dụ: Sai mật khẩu)
        this.errorMessage = err.error?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại!';
        console.error('Lỗi đăng nhập:', err);
      }
      
    });
  
  }

}