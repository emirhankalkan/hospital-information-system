import { Component, input } from '@angular/core';

import { PatientHealthSummaryItem } from '../patient-portal.types';

@Component({
  selector: 'app-patient-health-summary',
  standalone: true,
  templateUrl: './patient-health-summary.html',
  styleUrl: './patient-health-summary.scss',
})
export class PatientHealthSummaryComponent {
  readonly items = input.required<PatientHealthSummaryItem[]>();
}
