import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { 
    path: 'login', 
    loadComponent: () => import('./features/auth/login/login').then(m => m.Login) 
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN'] },
    loadComponent: () => import('./features/admin/dashboard/dashboard').then((m) => m.AdminDashboard),
  },
  {
    path: 'doctor',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_DOCTOR'] },
    loadComponent: () => import('./features/doctor/my-appointments/my-appointments').then((m) => m.DoctorAppointments),
  },
  {
    path: 'patient',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_PATIENT'] },
    loadComponent: () =>
      import('./features/patient-portal/my-appointments/my-appointments').then((m) => m.PatientAppointments),
  },
  {
    path: 'receptionist',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_RECEPTIONIST'] },
    loadComponent: () =>
      import('./features/receptionist/appointment-management/appointment-management').then(
        (m) => m.ReceptionistAppointmentManagement,
      ),
  }
];
