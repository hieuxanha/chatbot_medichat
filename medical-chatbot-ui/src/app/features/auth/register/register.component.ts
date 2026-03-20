import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isLoading = false;
  showPassword = false;

  constructor(private fb: FormBuilder, 
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[0-9]{10,11}$')]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      agreeTerms: [false, Validators.requiredTrue]
    }, { validators: this.passwordMatchValidator });
  }

  // Kiểm tra mật khẩu và xác nhận mật khẩu có khớp nhau không
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    return password && confirmPassword && password.value !== confirmPassword.value 
      ? { passwordMismatch: true } : null;
  }

  get f() { return this.registerForm.controls; }

 onSubmit(): void {
 
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    
    const registerData = {
      fullName: this.registerForm.value.fullName,
      email: this.registerForm.value.email, 
      password: this.registerForm.value.password,
      phone: this.registerForm.value.phone
    };

    
    this.authService.register(registerData).subscribe({
      next: (response) => {
        this.isLoading = false;
        alert('Đăng ký tài khoản thành công!');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isLoading = false;
        // Hiển thị lỗi từ Backend (ví dụ: Email đã tồn tại)
        alert(err.error?.message || 'Có lỗi xảy ra khi đăng ký!');
        console.error('Lỗi đăng ký:', err);
      },
    });
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}