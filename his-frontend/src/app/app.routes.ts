import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // --- Auth (herkese açık) ---
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password').then((m) => m.ForgotPassword),
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password').then((m) => m.ResetPassword),
  },
  {
    path: 'verify-email',
    loadComponent: () =>
      import('./features/auth/verify-email/verify-email').then((m) => m.VerifyEmail),
  },

  // --- Admin ---
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN'] },
    loadComponent: () =>
      import('./features/admin/dashboard/dashboard').then((m) => m.AdminDashboard),
  },

  // --- Doktor ---
  {
    path: 'doctor',
    loadComponent: () =>
      import('./features/doctor/doctor-layout/doctor-layout').then((m) => m.DoctorLayout),
    canActivate: [authGuard, roleGuard],
    canActivateChild: [authGuard, roleGuard],
    data: { roles: ['ROLE_DOCTOR'] },
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/doctor/dashboard/doctor-dashboard').then((m) => m.DoctorDashboard),
      },
      {
        path: 'appointments',
        loadComponent: () =>
          import('./features/doctor/my-appointments/my-appointments').then(
            (m) => m.DoctorAppointments,
          ),
      },
    ],
  },

  // --- Resepsiyonist ---
  {
    path: 'receptionist',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_RECEPTIONIST'] },
    loadComponent: () =>
      import('./features/receptionist/appointment-management/appointment-management').then(
        (m) => m.ReceptionistAppointmentManagement,
      ),
  },

  // --- Hasta Portalı (nested) ---
  // canActivate  → /patient'a ilk girişte guard çalışır
  // canActivateChild → /patient/profile, /patient/book gibi child geçişlerinde de çalışır
  // PatientLayout → sidebar + <router-outlet> shell bileşeni
  {
    path: 'patient',
    loadComponent: () =>
      import('./features/patient-portal/patient-layout/patient-layout').then(
        (m) => m.PatientLayout,
      ),
    canActivate: [authGuard, roleGuard],
    canActivateChild: [authGuard, roleGuard],
    data: { roles: ['ROLE_PATIENT'] },
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/patient-portal/my-appointments/my-appointments').then(
            (m) => m.PatientAppointments,
          ),
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/patient-portal/profile/patient-profile').then(
            (m) => m.PatientProfilePage,
          ),
      },
      {
        path: 'book',
        loadComponent: () =>
          import('./features/patient-portal/book-appointment/book-appointment').then(
            (m) => m.BookAppointment,
          ),
      },
    ],
  },
];
