import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localeTr from '@angular/common/locales/tr';
import Aura from '@primeuix/themes/aura';
import { providePrimeNG } from 'primeng/config';

import { authInterceptor } from './core/interceptors/auth.interceptor';
import { routes } from './app.routes';

registerLocaleData(localeTr);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimationsAsync(),
    { provide: LOCALE_ID, useValue: 'tr' },
    providePrimeNG({
      ripple: true,
      theme: {
        preset: Aura,
      },
    }),
  ],
};
