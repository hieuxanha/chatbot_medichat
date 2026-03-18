import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';

export const routes: Routes = [
  // Khi vào đường dẫn /login thì hiện LoginComponent
  { path: 'login', component: LoginComponent },
  
  // Khi vào trang chủ (rỗng) thì tự động chuyển hướng sang /login
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  
  // (Tùy chọn) Thêm path cho trang chat sau này
];