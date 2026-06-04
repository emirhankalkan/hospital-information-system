import { CommonModule, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';
import { finalize, forkJoin } from 'rxjs';

import { AppointmentService } from '../../../core/services/appointment.service';
import { DepartmentService } from '../../../core/services/department.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { PatientService } from '../../../core/services/patient.service';
import { AppointmentRequest } from '../../../models/appointment.models';
import { Department } from '../../../models/department.models';
import { Doctor } from '../../../models/doctor.models';
import { PatientProfile } from '../../../models/patient.models';

interface SelectOption<T> {
  label: string;
  value: T;
}

interface TimeOption extends SelectOption<string> {
  booked: boolean;
  disabled: boolean;
  statusLabel: string;
}

@Component({
  selector: 'app-book-appointment',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    ReactiveFormsModule,
    ButtonModule,
    DatePickerModule,
    InputTextModule,
    MessageModule,
    ProgressSpinnerModule,
    SelectModule,
    TextareaModule,
  ],
  templateUrl: './book-appointment.html',
  styleUrl: './book-appointment.scss',
})
export class BookAppointment implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly appointmentService = inject(AppointmentService);
  private readonly departmentService = inject(DepartmentService);
  private readonly doctorService = inject(DoctorService);
  private readonly patientService = inject(PatientService);

  readonly isLoading = signal(true);
  readonly isLoadingBookedTimes = signal(false);
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly profile = signal<PatientProfile | null>(null);
  readonly departments = signal<Department[]>([]);
  readonly doctors = signal<Doctor[]>([]);
  readonly bookedTimes = signal<string[]>([]);
  readonly selectedDepartmentId = signal<number | null>(null);
  readonly selectedDoctorId = signal<number | null>(null);
  readonly selectedAppointmentDate = signal<Date | null>(null);
  readonly minDate = new Date();

  private readonly workingHours = [
    '09:00',
    '09:30',
    '10:00',
    '10:30',
    '11:00',
    '11:30',
    '13:30',
    '14:00',
    '14:30',
    '15:00',
    '15:30',
    '16:00',
    '16:30',
  ];

  readonly departmentOptions = computed<SelectOption<number>[]>(() =>
    this.departments().map((department) => ({
      label: department.name,
      value: department.id,
    })),
  );
  readonly filteredDoctors = computed(() => {
    const departmentId = this.selectedDepartmentId();
    const doctors = this.doctors();

    if (!departmentId) {
      return doctors;
    }

    return doctors.filter((doctor) => doctor.departmentId === departmentId);
  });
  readonly doctorOptions = computed<SelectOption<number>[]>(() =>
    this.filteredDoctors().map((doctor) => ({
      label: this.getDoctorOptionLabel(doctor),
      value: doctor.id,
    })),
  );
  readonly selectedDoctor = computed(() =>
    this.doctors().find((doctor) => doctor.id === this.selectedDoctorId()) ?? null,
  );
  readonly timeOptions = computed<TimeOption[]>(() => {
    const bookedTimes = new Set(this.bookedTimes());

    return this.workingHours.map((time) => {
      const booked = bookedTimes.has(time);

      return {
        label: time,
        value: time,
        booked,
        disabled: booked,
        statusLabel: booked ? 'Dolu' : 'Müsait',
      };
    });
  });
  readonly canSelectTime = computed(() =>
    Boolean(this.selectedDoctorId() && this.selectedAppointmentDate() && !this.isLoadingBookedTimes()),
  );

  readonly appointmentForm = this.fb.group({
    departmentId: [null as number | null, Validators.required],
    doctorId: [null as number | null, Validators.required],
    appointmentDate: [null as Date | null, Validators.required],
    appointmentTime: [null as string | null, Validators.required],
    notes: ['', Validators.maxLength(500)],
  });

  ngOnInit(): void {
    this.watchAppointmentDate();
    this.loadPageData();
  }

  onDepartmentChange(departmentId: number | null): void {
    this.selectedDepartmentId.set(departmentId);
    this.selectedDoctorId.set(null);
    this.bookedTimes.set([]);
    this.appointmentForm.controls.doctorId.reset();
    this.appointmentForm.controls.appointmentTime.reset();
  }

  onDoctorChange(doctorId: number | null): void {
    this.selectedDoctorId.set(doctorId);
    this.appointmentForm.controls.appointmentTime.reset();
    this.loadBookedTimes();
  }

  bookAppointment(): void {
    const profile = this.profile();

    if (!profile || this.appointmentForm.invalid) {
      this.appointmentForm.markAllAsTouched();
      return;
    }

    const rawValue = this.appointmentForm.getRawValue();
    const request: AppointmentRequest = {
      patientId: profile.id,
      doctorId: rawValue.doctorId as number,
      appointmentDate: this.formatDate(rawValue.appointmentDate as Date),
      appointmentTime: rawValue.appointmentTime as string,
      notes: this.emptyToNull(rawValue.notes),
    };

    this.errorMessage.set('');
    this.successMessage.set('');
    this.isSubmitting.set(true);

    this.appointmentService
      .create(request)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Randevunuz başarıyla oluşturuldu. Randevularım sayfasına yönlendiriliyorsunuz.');
          setTimeout(() => void this.router.navigate(['/patient']), 900);
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage.set(error.error?.message ?? 'Randevu oluşturulamadı.');
        },
      });
  }

  getFieldError(controlName: keyof typeof this.appointmentForm.controls): string {
    const control = this.appointmentForm.controls[controlName];

    if (!control.touched || !control.invalid) {
      return '';
    }

    if (control.errors?.['required']) {
      return 'Bu alan zorunludur.';
    }

    if (control.errors?.['maxlength']) {
      return 'En fazla 500 karakter girilebilir.';
    }

    return 'Alan değerini kontrol edin.';
  }

  private loadPageData(): void {
    forkJoin({
      profile: this.patientService.getMyProfile(),
      departments: this.departmentService.getAll(),
      doctors: this.doctorService.getAll(),
    })
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: ({ profile, departments, doctors }) => {
          this.profile.set(profile);
          this.departments.set(departments);
          this.doctors.set(doctors);
          this.selectedDepartmentId.set(null);
          this.selectedDoctorId.set(null);
          this.selectedAppointmentDate.set(null);
          this.bookedTimes.set([]);
          this.appointmentForm.reset({
            departmentId: null,
            doctorId: null,
            appointmentDate: null,
            appointmentTime: null,
            notes: '',
          });
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage.set(error.error?.message ?? 'Randevu sayfası verileri getirilemedi.');
        },
      });
  }

  private watchAppointmentDate(): void {
    this.appointmentForm.controls.appointmentDate.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((date) => {
        this.selectedAppointmentDate.set(date);
        this.appointmentForm.controls.appointmentTime.reset();
        this.loadBookedTimes();
      });
  }

  private loadBookedTimes(): void {
    const doctorId = this.selectedDoctorId();
    const date = this.selectedAppointmentDate();

    this.bookedTimes.set([]);

    if (!doctorId || !date) {
      this.isLoadingBookedTimes.set(false);
      return;
    }

    this.isLoadingBookedTimes.set(true);
    this.appointmentService
      .getBookedTimes(doctorId, this.formatDate(date))
      .pipe(finalize(() => this.isLoadingBookedTimes.set(false)))
      .subscribe({
        next: (bookedTimes) => {
          this.bookedTimes.set(bookedTimes);

          const selectedTime = this.appointmentForm.controls.appointmentTime.value;
          if (selectedTime && bookedTimes.includes(selectedTime)) {
            this.appointmentForm.controls.appointmentTime.reset();
          }
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage.set(error.error?.message ?? 'Dolu saatler getirilemedi.');
        },
      });
  }

  private getDoctorOptionLabel(doctor: Doctor): string {
    const name = `Dr. ${doctor.firstName} ${doctor.lastName}`.trim();
    const detail = doctor.specialization || doctor.departmentName;

    return detail ? `${name} - ${detail}` : name;
  }

  private formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  private emptyToNull(value: string | null | undefined): string | null {
    const trimmed = value?.trim();
    return trimmed ? trimmed : null;
  }
}
