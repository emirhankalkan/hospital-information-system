import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.models';
import { Doctor } from '../../models/doctor.models';

@Injectable({ providedIn: 'root' })
export class DoctorService {
  private readonly http = inject(HttpClient);
  private readonly doctorsUrl = `${environment.apiUrl}/doctors`;

  getMyProfile(): Observable<Doctor> {
    return this.http
      .get<ApiResponse<Doctor>>(`${this.doctorsUrl}/me`)
      .pipe(map((r) => r.data));
  }

  getAll(): Observable<Doctor[]> {
    return this.http
      .get<ApiResponse<Doctor[]>>(this.doctorsUrl)
      .pipe(map((response) => response.data ?? []));
  }

  getByDepartmentId(departmentId: number): Observable<Doctor[]> {
    return this.http
      .get<ApiResponse<Doctor[]>>(`${this.doctorsUrl}/department/${departmentId}`)
      .pipe(map((response) => response.data ?? []));
  }
}
