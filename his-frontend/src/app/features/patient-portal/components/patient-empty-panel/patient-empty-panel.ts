import { Component, input } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';

import { PatientEmptyPanelConfig } from '../patient-portal.types';

@Component({
  selector: 'app-patient-empty-panel',
  standalone: true,
  imports: [ButtonModule, TagModule],
  templateUrl: './patient-empty-panel.html',
  styleUrl: './patient-empty-panel.scss',
})
export class PatientEmptyPanelComponent {
  readonly config = input.required<PatientEmptyPanelConfig>();
}
