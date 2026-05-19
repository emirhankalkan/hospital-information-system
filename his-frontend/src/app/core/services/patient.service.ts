import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.models';
import { PatientProfile } from '../../models/patient.models';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly http = inject(HttpClient);
  private readonly patientsUrl = `${environment.apiUrl}/patients`;

  getMyProfile(): Observable<PatientProfile> {
    return this.http
      .get<ApiResponse<PatientProfile>>(`${this.patientsUrl}/me`)
      .pipe(map((response) => response.data));
  }
}
