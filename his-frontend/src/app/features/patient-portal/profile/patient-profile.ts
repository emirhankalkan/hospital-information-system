import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';

import { PatientService } from '../../../core/services/patient.service';
import { PatientProfile, PatientProfileUpdateRequest } from '../../../models/patient.models';

@Component({
  selector: 'app-patient-profile-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    SelectModule,
    DatePickerModule,
    TextareaModule,
    MessageModule,
    ProgressSpinnerModule,
  ],
  templateUrl: './patient-profile.html',
  styleUrl: './patient-profile.scss',
})
export class PatientProfilePage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly patientService = inject(PatientService);

  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly profile = signal<PatientProfile | null>(null);

  readonly genderOptions = [
    { label: 'Kadın', value: 'FEMALE' },
    { label: 'Erkek', value: 'MALE' },
    { label: 'Diğer', value: 'OTHER' },
  ];

  readonly today = new Date();

  readonly bloodTypeOptions = [
    { label: 'A+', value: 'A+' },
    { label: 'A-', value: 'A-' },
    { label: 'B+', value: 'B+' },
    { label: 'B-', value: 'B-' },
    { label: 'AB+', value: 'AB+' },
    { label: 'AB-', value: 'AB-' },
    { label: '0+', value: '0+' },
    { label: '0-', value: '0-' },
  ];

  readonly profileForm = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    tcNo: [{ value: '', disabled: true }],
    birthDate: [null as Date | null],
    gender: [null as 'MALE' | 'FEMALE' | 'OTHER' | null],
    phone: ['', [Validators.pattern(/^05\d{2} \d{3} \d{2} \d{2}$/)]],
    email: ['', [Validators.email, Validators.maxLength(100)]],
    bloodType: [null as string | null],
    emergencyContact: ['', [Validators.maxLength(100)]],
    address: [''],
  });

  ngOnInit(): void {
    this.patientService.getMyProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.profileForm.patchValue({
          firstName: profile.firstName,
          lastName: profile.lastName,
          tcNo: profile.tcNo ?? '',
          birthDate: profile.birthDate ? new Date(profile.birthDate) : null,
          gender: profile.gender ?? null,
          phone: this.formatTurkishPhone(profile.phone ?? ''),
          email: profile.email ?? '',
          bloodType: profile.bloodType ?? null,
          emergencyContact: profile.emergencyContact ?? '',
          address: profile.address ?? '',
        });
        this.isLoading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'Profil bilgileri getirilemedi.');
        this.isLoading.set(false);
      },
    });
  }

  saveProfile(): void {
    const profile = this.profile();
    if (!profile || this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const rawValue = this.profileForm.getRawValue();
    const request: PatientProfileUpdateRequest = {
      firstName: rawValue.firstName ?? '',
      lastName: rawValue.lastName ?? '',
      tcNo: profile.tcNo ?? null,
      birthDate: rawValue.birthDate ? this.formatDate(rawValue.birthDate) : null,
      gender: rawValue.gender ?? null,
      phone: this.emptyToNull(rawValue.phone),
      email: this.emptyToNull(rawValue.email),
      bloodType: rawValue.bloodType ?? null,
      emergencyContact: this.emptyToNull(rawValue.emergencyContact),
      address: this.emptyToNull(rawValue.address),
    };

    this.errorMessage.set('');
    this.successMessage.set('');
    this.isSaving.set(true);

    this.patientService.updateProfile(profile.id, request).subscribe({
      next: (updatedProfile) => {
        this.profile.set(updatedProfile);
        this.successMessage.set('Profil bilgileriniz başarıyla güncellendi.');
        this.isSaving.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'Profil güncellenemedi.');
        this.isSaving.set(false);
      },
    });
  }

  private formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  formatPhoneInput(): void {
    const phoneControl = this.profileForm.controls.phone;
    const formattedPhone = this.formatTurkishPhone(phoneControl.value ?? '');

    if (phoneControl.value !== formattedPhone) {
      phoneControl.setValue(formattedPhone, { emitEvent: false });
    }
  }

  private formatTurkishPhone(value: string): string {
    const digits = value.replace(/\D/g, '').slice(0, 11);
    const groups = [
      digits.slice(0, 4),
      digits.slice(4, 7),
      digits.slice(7, 9),
      digits.slice(9, 11),
    ].filter(Boolean);

    return groups.join(' ');
  }

  private emptyToNull(value: string | null | undefined): string | null {
    const trimmed = value?.trim();
    return trimmed ? trimmed : null;
  }
}
