import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';

export type NovaraTheme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly state = signal<NovaraTheme>('dark');
  readonly theme = this.state.asReadonly();

  constructor() {
    this.apply('dark');
  }

  toggle(): void {
    this.apply(this.state() === 'dark' ? 'light' : 'dark');
  }

  setTheme(theme: NovaraTheme): void {
    this.apply(theme);
  }

  private apply(theme: NovaraTheme): void {
    this.state.set(theme);
    this.document.documentElement.dataset['theme'] = theme;
  }
}
