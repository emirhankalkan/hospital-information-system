import { DatePipe } from '@angular/common';
import { Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';

import { Appointment, AppointmentStatus } from '../../../../models/appointment.models';
import { PatientTagSeverity } from '../patient-portal.types';

@Component({
  selector: 'app-patient-appointments-panel',
  standalone: true,
  imports: [ButtonModule, DatePipe, RouterLink, TagModule],
  templateUrl: './patient-appointments-panel.html',
  styleUrl: './patient-appointments-panel.scss',
})
export class PatientAppointmentsPanelComponent {
  readonly appointments = input.required<Appointment[]>();
  readonly cancelingAppointmentId = input<number | null>(null);
  readonly cancelAppointment = output<number>();
  readonly upcomingAppointments = computed(() =>
    this.appointments()
      .filter((appointment) => appointment.status === 'SCHEDULED')
      .slice(0, 3),
  );
  readonly pastAppointments = computed(() =>
    this.appointments()
      .filter((appointment) => appointment.status !== 'SCHEDULED')
      .slice(0, 2),
  );

  getDoctorName(appointment: Appointment): string {
    return `Dr. ${appointment.doctorFirstName} ${appointment.doctorLastName}`.trim();
  }

  getStatusLabel(status: AppointmentStatus): string {
    const labels: Record<AppointmentStatus, string> = {
      SCHEDULED: 'Planlandı',
      COMPLETED: 'Tamamlandı',
      CANCELED: 'İptal edildi',
    };

    return labels[status];
  }

  getStatusSeverity(status: AppointmentStatus): PatientTagSeverity {
    const severities: Record<AppointmentStatus, PatientTagSeverity> = {
      SCHEDULED: 'info',
      COMPLETED: 'success',
      CANCELED: 'danger',
    };

    return severities[status];
  }

  onCancelAppointment(appointmentId: number): void {
    this.cancelAppointment.emit(appointmentId);
  }
}
