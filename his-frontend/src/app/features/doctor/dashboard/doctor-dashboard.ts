import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { switchMap } from 'rxjs';

import { AppointmentService } from '../../../core/services/appointment.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { Appointment, AppointmentStatus } from '../../../models/appointment.models';
import { Doctor } from '../../../models/doctor.models';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [AvatarModule, DatePipe, ProgressSpinnerModule, RouterLink, TagModule],
  templateUrl: './doctor-dashboard.html',
  styleUrl: './doctor-dashboard.scss',
})
export class DoctorDashboard implements OnInit {
  private readonly doctorService = inject(DoctorService);
  private readonly appointmentService = inject(AppointmentService);

  readonly isLoading = signal(true);
  readonly doctor = signal<Doctor | null>(null);
  readonly appointments = signal<Appointment[]>([]);
  readonly today = new Date();

  readonly todayStr = computed(() => {
    const d = this.today;
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  });

  readonly todayAppointments = computed(() =>
    this.appointments().filter((a) => a.appointmentDate === this.todayStr()),
  );

  readonly upcomingCount = computed(
    () => this.appointments().filter((a) => a.status === 'SCHEDULED').length,
  );

  readonly completedToday = computed(
    () => this.todayAppointments().filter((a) => a.status === 'COMPLETED').length,
  );

  readonly pendingToday = computed(
    () => this.todayAppointments().filter((a) => a.status === 'SCHEDULED').length,
  );

  ngOnInit(): void {
    this.doctorService
      .getMyProfile()
      .pipe(
        switchMap((doc) => {
          this.doctor.set(doc);
          return this.appointmentService.getByDoctorId(doc.id);
        }),
      )
      .subscribe({
        next: (appts) => {
          this.appointments.set(appts);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
  }

  getStatusLabel(status: AppointmentStatus): string {
    const map: Record<AppointmentStatus, string> = {
      SCHEDULED: 'Planlandı',
      COMPLETED: 'Tamamlandı',
      CANCELED: 'İptal',
    };
    return map[status] ?? status;
  }

  getStatusSeverity(status: AppointmentStatus): 'success' | 'info' | 'danger' {
    const map: Record<AppointmentStatus, 'success' | 'info' | 'danger'> = {
      SCHEDULED: 'info',
      COMPLETED: 'success',
      CANCELED: 'danger',
    };
    return map[status] ?? 'info';
  }

  patientName(a: Appointment): string {
    return `${a.patientFirstName} ${a.patientLastName}`.trim();
  }

  patientInitials(a: Appointment): string {
    return ((a.patientFirstName[0] ?? '') + (a.patientLastName[0] ?? '')).toUpperCase() || '?';
  }
}
