import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { PatientProfile } from '../../../models/patient.models';
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
    PatientEmptyPanelComponent,
    PatientHealthSummaryComponent,
    PatientHeroComponent,
    PatientStatusCardsComponent,
  ],
  templateUrl: './my-appointments.html',
  styleUrl: './my-appointments.scss',
})
export class PatientAppointments implements OnInit {
  private readonly patientService = inject(PatientService);
  readonly authService = inject(AuthService);

  readonly isLoading = signal(true);
  readonly errorMessage = signal('');
  readonly profile = signal<PatientProfile | null>(null);
  readonly today = new Intl.DateTimeFormat('tr-TR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  }).format(new Date());
  readonly statusCards: PatientStatusCard[] = [
    {
      icon: 'pi-calendar-clock',
      label: 'Yaklaşan randevu',
      value: 'Henüz yok',
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
  ];
  readonly appointmentPanel: PatientEmptyPanelConfig = {
    kicker: 'Randevular',
    title: 'Randevularım',
    icon: 'pi-calendar',
    heading: 'Yaklaşan randevunuz bulunmuyor',
    description: 'Randevu alma akışı bağlandığında doktor, bölüm, tarih ve durum bilgileri burada listelenecek.',
    actionLabel: 'Randevu Al',
    actionIcon: 'pi pi-calendar-plus',
    large: true,
  };
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
        value: profile?.birthDate || 'Eklenmedi',
      },
      {
        label: 'Telefon',
        value: profile?.phone || 'Eklenmedi',
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
    this.patientService.getMyProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.isLoading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.error?.message ?? 'Hasta profili getirilemedi.');
        this.isLoading.set(false);
      },
    });
  }
}
