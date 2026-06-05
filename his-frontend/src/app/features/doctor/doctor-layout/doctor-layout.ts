import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { TooltipModule } from 'primeng/tooltip';
import { filter } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  exact: boolean;
}

@Component({
  selector: 'app-doctor-layout',
  standalone: true,
  imports: [AvatarModule, RouterLink, RouterLinkActive, RouterOutlet, TooltipModule],
  templateUrl: './doctor-layout.html',
  styleUrl: './doctor-layout.scss',
})
export class DoctorLayout implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly doctorService = inject(DoctorService);
  private readonly router = inject(Router);

  readonly isCollapsed = signal(false);
  readonly isMobileSidebarOpen = signal(false);

  readonly firstName = signal('');
  readonly lastName = signal('');
  readonly specialization = signal('');

  readonly initials = computed(() => {
    const f = this.firstName()[0] ?? '';
    const l = this.lastName()[0] ?? '';
    return (f + l).toUpperCase() || 'DR';
  });

  readonly displayName = computed(() =>
    `${this.firstName()} ${this.lastName()}`.trim() || '...',
  );

  readonly navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'pi pi-home', route: '/doctor', exact: true },
    { label: 'Randevularım', icon: 'pi pi-calendar', route: '/doctor/appointments', exact: false },
  ];

  constructor() {
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.isMobileSidebarOpen.set(false));
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  private loadProfile(): void {
    this.doctorService.getMyProfile().subscribe({
      next: (doc) => {
        this.firstName.set(doc.firstName ?? '');
        this.lastName.set(doc.lastName ?? '');
        this.specialization.set(doc.specialization ?? doc.departmentName ?? '');
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
