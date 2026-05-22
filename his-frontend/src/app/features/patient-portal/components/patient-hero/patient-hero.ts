import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';

import { PatientTagSeverity } from '../patient-portal.types';

@Component({
  selector: 'app-patient-hero',
  standalone: true,
  imports: [AvatarModule, ButtonModule, RouterLink, TagModule],
  templateUrl: './patient-hero.html',
  styleUrl: './patient-hero.scss',
})
export class PatientHeroComponent {
  readonly displayName = input.required<string>();
  readonly initials = input.required<string>();
  readonly patientId = input<number | null>(null);
  readonly lastLoginLabel = input.required<string>();
  readonly profileStatusLabel = input.required<string>();
  readonly profileStatusSeverity = input.required<PatientTagSeverity>();
  readonly profileRoute = input('/patient/profile');
}
