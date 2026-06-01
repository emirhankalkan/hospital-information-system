import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { finalize, switchMap, tap } from 'rxjs';

import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { Appointment } from '../../../models/appointment.models';
import { PatientProfile } from '../../../models/patient.models';
import { PatientAppointmentsPanelComponent } from '../components/patient-appointments-panel/patient-appointments-panel';
import { PatientEmptyPanelComponent } from '../components/patient-empty-panel/patient-empty-panel';
import { PatientHealthSummaryComponent } from '../components/patient-health-summary/patient-health-summary';
import { PatientHeroComponent } from '../components/patient-hero/patient-hero';
import { PatientStatusCardsComponent } from '../components/patient-status-cards/patient-status-cards';
import {
  PatientEmptyPanelConfig,
  PatientHealthSummaryItem,
  PatientStatusCard,
  PatientTagSeverity,
} from '../components/patient-portal.types';

@Component({
  selector: 'app-patient-appointments',
  standalone: true,
  imports: [
    CommonModule,
    MessageModule,
    ProgressSpinnerModule,
    PatientAppointmentsPanelComponent,
    PatientEmptyPanelComponent,
    PatientHealthSummaryComponent,
    PatientHeroComponent,
    PatientStatusCardsComponent,
  ],
  templateUrl: './my-appointments.html',
  styleUrl: './my-appointments.scss',
})
export class PatientAppointments implements OnInit {
  private readonly appointmentService = inject(AppointmentService);
  private readonly patientService = inject(PatientService);
  private readonly authService = inject(AuthService);

  readonly isLoading = signal(true);
  readonly errorMessage = signal('');
  readonly appointmentErrorMessage = signal('');
  readonly appointmentSuccessMessage = signal('');
  readonly cancelingAppointmentId = signal<number | null>(null);
  readonly appointments = signal<Appointment[]>([]);
  readonly profile = signal<PatientProfile | null>(null);
  readonly today = new Intl.DateTimeFormat('tr-TR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  }).format(new Date());
  readonly detailPanels: PatientEmptyPanelConfig[] = [
    {
      kicker: 'Tahlil ve Sonuçlar',
      title: 'Sonuçlarım',
      tagLabel: '0 sonuç',
      tagSeverity: 'secondary',
      icon: 'pi-file-pdf',
      description: 'Henüz görüntülenebilir tahlil veya radyoloji sonucu yok.',
    },
    {
      kicker: 'Reçeteler',
      title: 'İlaçlarım',
      tagLabel: '0 aktif',
      tagSeverity: 'secondary',
      icon: 'pi-heart',
      description: 'Aktif reçete bilgisi geldiğinde ilaç adı, doz ve kullanım süresi burada görünecek.',
    },
    {
      kicker: 'Bildirimler',
      title: 'Hasta Bildirimleri',
      icon: 'pi-info-circle',
      description: 'Yeni tahlil sonucu, randevu hatırlatması ve doktor notları bu alanda gösterilecek.',
    },
  ];

  readonly displayName = computed(() => {
    const profile = this.profile();
    if (!profile) {
      return 'Hasta';
    }

    return `${profile.firstName} ${profile.lastName}`.trim();
  });
  readonly initials = computed(() =>
    this.displayName()
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toLocaleUpperCase('tr-TR'))
      .join(''),
  );
  readonly isProfileIncomplete = computed(() => {
    const profile = this.profile();
    return Boolean(profile && (!profile.tcNo || !profile.phone || !profile.birthDate));
  });
  readonly scheduledAppointments = computed(() =>
    this.appointments().filter((appointment) => appointment.status === 'SCHEDULED'),
  );
  readonly statusCards = computed<PatientStatusCard[]>(() => [
    {
      icon: 'pi-calendar-clock',
      label: 'Yaklaşan randevu',
      value:
        this.scheduledAppointments().length > 0
          ? `${this.scheduledAppointments().length} randevu`
          : 'Henüz yok',
    },
    {
      icon: 'pi-file-check',
      label: 'Bekleyen sonuç',
      value: '0 sonuç',
    },
    {
      icon: 'pi-clipboard',
      label: 'Aktif reçete',
      value: '0 reçete',
    },
    {
      icon: 'pi-bell',
      label: 'Bildirim',
      value: 'Yeni bildirim yok',
    },
  ]);
  readonly profileCompletionLabel = computed(() =>
    this.isProfileIncomplete() ? 'Profil tamamlanmalı' : 'Profil tamamlandı',
  );
  readonly profileCompletionSeverity = computed<PatientTagSeverity>(() =>
    this.isProfileIncomplete() ? 'warn' : 'success',
  );
  readonly healthSummaryItems = computed<PatientHealthSummaryItem[]>(() => {
    const profile = this.profile();

    return [
      {
        label: 'Kan grubu',
        value: profile?.bloodType || 'Eklenmedi',
      },
      {
        label: 'Doğum tarihi',
        value: this.formatBirthDate(profile?.birthDate),
      },
      {
        label: 'Telefon',
        value: this.formatPhone(profile?.phone),
      },
      {
        label: 'E-posta',
        value: profile?.email || this.authService.currentUser()?.email || 'Eklenmedi',
      },
      {
        label: 'TC Kimlik No',
        value: profile?.tcNo || 'Eklenmedi',
      },
      {
        label: 'Acil durum kişisi',
        value: profile?.emergencyContact || 'Eklenmedi',
      },
    ];
  });

  ngOnInit(): void {
    this.loadPatientDashboard();
  }

  logout(): void {
    this.authService.logout();
  }

  cancelAppointment(appointmentId: number): void {
    this.appointmentErrorMessage.set('');
    this.appointmentSuccessMessage.set('');
    this.cancelingAppointmentId.set(appointmentId);

    this.appointmentService
      .cancelAppointment(appointmentId)
      .pipe(
        switchMap(() => {
          const patientId = this.profile()?.id;
          if (!patientId) {
            throw new Error('Hasta profili bulunamadı.');
          }

          return this.appointmentService.getByPatientId(patientId);
        }),
        finalize(() => this.cancelingAppointmentId.set(null)),
      )
      .subscribe({
        next: (appointments) => {
          this.appointments.set(appointments);
          this.appointmentSuccessMessage.set('Randevu iptal edildi.');
        },
        error: (error: HttpErrorResponse) => {
          this.appointmentErrorMessage.set(
            error.error?.message ?? 'Randevu iptal edilemedi.',
          );
        },
      });
  }

  private loadPatientDashboard(): void {
    this.patientService
      .getMyProfile()
      .pipe(
        tap((profile) => this.profile.set(profile)),
        switchMap((profile) => this.appointmentService.getByPatientId(profile.id)),
        finalize(() => this.isLoading.set(false)),
      )
      .subscribe({
        next: (appointments) => {
          this.appointments.set(appointments);
        },
        error: (error: HttpErrorResponse) => {
          if (this.profile()) {
            this.appointmentErrorMessage.set(
              error.error?.message ?? 'Randevu bilgileri getirilemedi.',
            );
            return;
          }

          this.errorMessage.set(error.error?.message ?? 'Hasta profili getirilemedi.');
        },
      });
  }

  /** yyyy-MM-dd → 12 Haziran 1992 */
  private formatBirthDate(dateStr: string | null | undefined): string {
    if (!dateStr) return 'Eklenmedi';
    try {
      const date = new Date(dateStr);
      return new Intl.DateTimeFormat('tr-TR', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
      }).format(date);
    } catch {
      return dateStr;
    }
  }

  /** 5551234567 → 0555 123 45 67 */
  private formatPhone(phone: string | null | undefined): string {
    if (!phone) return 'Eklenmedi';
    const digits = phone.replace(/\D/g, '');
    // 10 haneli (başında 0 olmadan)
    if (digits.length === 10) {
      return `0${digits.slice(0, 3)} ${digits.slice(3, 6)} ${digits.slice(6, 8)} ${digits.slice(8)}`;
    }
    // 11 haneli (0 ile başlıyor)
    if (digits.length === 11 && digits.startsWith('0')) {
      return `${digits.slice(0, 4)} ${digits.slice(4, 7)} ${digits.slice(7, 9)} ${digits.slice(9)}`;
    }
    return phone;
  }
}
