import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';

export const routes: Routes = [
  // Khi vào đường dẫn /login thì hiện LoginComponent
  { path: 'login', component: LoginComponent },

  { path: 'register', component: RegisterComponent },

  
  
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  
  //   nguyen cong hieu 
];