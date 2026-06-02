import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.models';
import { Department } from '../../models/department.models';

@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private readonly http = inject(HttpClient);
  private readonly departmentsUrl = `${environment.apiUrl}/departments`;

  getAll(): Observable<Department[]> {
    return this.http
      .get<ApiResponse<Department[]>>(this.departmentsUrl)
      .pipe(map((response) => response.data ?? []));
  }
}
