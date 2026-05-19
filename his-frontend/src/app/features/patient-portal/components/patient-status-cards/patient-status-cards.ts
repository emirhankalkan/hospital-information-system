import { Component, input } from '@angular/core';

import { PatientStatusCard } from '../patient-portal.types';

@Component({
  selector: 'app-patient-status-cards',
  standalone: true,
  templateUrl: './patient-status-cards.html',
  styleUrl: './patient-status-cards.scss',
})
export class PatientStatusCardsComponent {
  readonly cards = input.required<PatientStatusCard[]>();
}
