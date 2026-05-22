import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.models';
import {
  Appointment,
  AppointmentRequest,
  AppointmentStatusUpdateRequest,
} from '../../models/appointment.models';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly http = inject(HttpClient);
  private readonly appointmentsUrl = `${environment.apiUrl}/appointments`;

  /** Hastanın kendi randevularını getirir */
  getByPatientId(patientId: number): Observable<Appointment[]> {
    return this.http
      .get<ApiResponse<Appointment[]>>(`${this.appointmentsUrl}/patient/${patientId}`)
      .pipe(map((response) => response.data ?? []));
  }

  /** Doktorun kendi randevularını getirir */
  getByDoctorId(doctorId: number): Observable<Appointment[]> {
    return this.http
      .get<ApiResponse<Appointment[]>>(`${this.appointmentsUrl}/doctor/${doctorId}`)
      .pipe(map((response) => response.data ?? []));
  }

  /** Tüm randevuları getirir (Admin / Resepsiyonist) */
  getAll(): Observable<Appointment[]> {
    return this.http
      .get<ApiResponse<Appointment[]>>(this.appointmentsUrl)
      .pipe(map((response) => response.data ?? []));
  }

  /** Yeni randevu oluşturur */
  create(request: AppointmentRequest): Observable<Appointment> {
    return this.http
      .post<ApiResponse<Appointment>>(this.appointmentsUrl, request)
      .pipe(map((response) => response.data));
  }

  /** Randevu durumunu günceller (COMPLETED, CANCELED vb.) */
  updateStatus(appointmentId: number, request: AppointmentStatusUpdateRequest): Observable<Appointment> {
    return this.http
      .patch<ApiResponse<Appointment>>(`${this.appointmentsUrl}/${appointmentId}/status`, request)
      .pipe(map((response) => response.data));
  }

  /** Randevuyu iptal eder (hasta / resepsiyonist) */
  cancelAppointment(appointmentId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.appointmentsUrl}/${appointmentId}`);
  }
}
