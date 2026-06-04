import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { TooltipModule } from 'primeng/tooltip';
import { filter } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  exact: boolean;
}

interface ComingSoonItem {
  label: string;
  icon: string;
}

@Component({
  selector: 'app-patient-layout',
  standalone: true,
  imports: [
    AvatarModule,
    ButtonModule,
    DividerModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    TooltipModule,
  ],
  templateUrl: './patient-layout.html',
  styleUrl: './patient-layout.scss',
})
export class PatientLayout implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly patientService = inject(PatientService);
  private readonly router = inject(Router);

  readonly isCollapsed = signal(false);
  readonly isMobileSidebarOpen = signal(false);

  readonly firstName = signal('');
  readonly lastName = signal('');

  readonly displayName = computed(() => {
    const first = this.firstName();
    const last = this.lastName();
    return first || last ? `${first} ${last}`.trim() : '';
  });

  /** Teknik kullanıcı adı mı? (nokta + alfanümerik → ör: emikalkann12.mpbav2ol) */
  readonly isTechnicalName = computed(() => {
    const name = this.firstName();
    return name.length > 0 && /[a-z0-9]+\.[a-z0-9]{4,}/i.test(name);
  });

  /** Sidebar'da güvenli görüntüleme — teknik ise boş döner */
  readonly safeFirstName = computed(() =>
    this.isTechnicalName() ? '' : this.firstName(),
  );

  readonly initials = computed(() => {
    if (this.isTechnicalName()) return '?';
    const f = this.firstName()[0] ?? '';
    const l = this.lastName()[0] ?? '';
    return (f + l).toUpperCase() || '?';
  });

  readonly navItems: NavItem[] = [
    { label: 'Randevularım', icon: 'pi pi-home', route: '/patient', exact: true },
    { label: 'Randevu Al', icon: 'pi pi-calendar-plus', route: '/patient/book', exact: false },
    { label: 'Profilim', icon: 'pi pi-user', route: '/patient/profile', exact: false },
  ];

  readonly comingSoonItems: ComingSoonItem[] = [
    { label: 'Tahlillerim', icon: 'pi pi-file-check' },
    { label: 'Reçetelerim', icon: 'pi pi-bookmark' },
    { label: 'Bildirimler', icon: 'pi pi-bell' },
  ];

  constructor() {
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => {
        this.isMobileSidebarOpen.set(false);
        this.loadProfile();
      });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  private loadProfile(): void {
    this.patientService.getMyProfile().subscribe({
      next: (profile) => {
        this.firstName.set(profile.firstName ?? '');
        this.lastName.set(profile.lastName ?? '');
      },
    });
  }

  toggleCollapse(): void {
    this.isCollapsed.update((v) => !v);
  }

  toggleMobileSidebar(): void {
    this.isMobileSidebarOpen.update((v) => !v);
  }

  closeMobileSidebar(): void {
    this.isMobileSidebarOpen.set(false);
  }

  logout(): void {
    this.authService.logout();
  }
}
