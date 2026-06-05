import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { switchMap } from 'rxjs';
import { AvatarModule } from 'primeng/avatar';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';

import { AppointmentService } from '../../../core/services/appointment.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { Appointment, AppointmentStatus } from '../../../models/appointment.models';

type FilterStatus = 'ALL' | AppointmentStatus;

@Component({
  selector: 'app-doctor-appointments',
  standalone: true,
  imports: [
    AvatarModule, ButtonModule, DatePipe, DatePickerModule,
    FormsModule, MessageModule, ProgressSpinnerModule,
    SelectModule, TagModule, TooltipModule,
  ],
  templateUrl: './my-appointments.html',
  styleUrl: './my-appointments.scss',
})
export class DoctorAppointments implements OnInit {
  private readonly doctorService = inject(DoctorService);
  private readonly appointmentService = inject(AppointmentService);

  readonly isLoading = signal(true);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly appointments = signal<Appointment[]>([]);
  readonly updatingId = signal<number | null>(null);

  readonly filterStatus = signal<FilterStatus>('ALL');
  readonly filterDate = signal<Date | null>(null);

  readonly statusOptions = [
    { label: 'Tümü', value: 'ALL' },
    { label: 'Planlandı', value: 'SCHEDULED' },
    { label: 'Tamamlandı', value: 'COMPLETED' },
    { label: 'İptal', value: 'CANCELED' },
  ];

  readonly filtered = computed(() => {
    let list = this.appointments();
    const status = this.filterStatus();
    const date = this.filterDate();

    if (status !== 'ALL') {
      list = list.filter((a) => a.status === status);
    }
    if (date) {
      const dateStr = this.toDateStr(date);
      list = list.filter((a) => a.appointmentDate === dateStr);
    }
    return [...list].sort((a, b) => {
      const d = a.appointmentDate.localeCompare(b.appointmentDate);
      return d !== 0 ? d : a.appointmentTime.localeCompare(b.appointmentTime);
    });
  });

  ngOnInit(): void {
    this.doctorService
      .getMyProfile()
      .pipe(switchMap((doc) => this.appointmentService.getByDoctorId(doc.id)))
      .subscribe({
        next: (appts) => {
          this.appointments.set(appts);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
  }

  markCompleted(appt: Appointment): void {
    this.updateStatus(appt, 'COMPLETED');
  }

  markCanceled(appt: Appointment): void {
    this.updateStatus(appt, 'CANCELED');
  }

  private updateStatus(appt: Appointment, status: AppointmentStatus): void {
    this.updatingId.set(appt.id);
    this.errorMessage.set('');
    this.successMessage.set('');

    this.appointmentService.updateStatus(appt.id, { status }).subscribe({
      next: (updated) => {
        this.appointments.update((list) =>
          list.map((a) => (a.id === updated.id ? updated : a)),
        );
        this.successMessage.set('Randevu durumu güncellendi.');
        this.updatingId.set(null);
        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: () => {
        this.errorMessage.set('Durum güncellenirken hata oluştu.');
        this.updatingId.set(null);
      },
    });
  }

  clearFilters(): void {
    this.filterStatus.set('ALL');
    this.filterDate.set(null);
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

  private toDateStr(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
}
