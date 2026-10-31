import { DOCUMENT } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';

export type NovaraLogoVariant = 'auto' | 'light' | 'dark';

@Component({
  selector: 'novara-logo',
  host: { '[class.compact]': 'compact()', '[attr.data-variant]': 'variant()' },
  template: `@if (compact()) {
      <img class="icon" src="/novara-icon.png" width="512" height="512" alt="Novara Bank" />
    } @else {
      <img class="logo" [src]="logoSource()" width="1200" height="409" alt="Novara Bank" />
    }`,
  styles: [
    `
      :host {
        display: inline-flex;
        flex: 0 0 auto;
        align-items: center;
      }
      img {
        display: block;
        max-width: 100%;
        object-fit: contain;
      }
      .logo {
        width: 9.8rem;
        height: auto;
      }
      .icon {
        width: 2.55rem;
        height: 2.55rem;
        border-radius: 0.68rem;
      }
      :host(.compact) {
        flex: 0 0 auto;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LogoComponent {
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private readonly automaticVariant = signal<'light' | 'dark'>('dark');
  readonly compact = input(false);
  readonly variant = input<NovaraLogoVariant>('auto');
  protected readonly logoSource = computed(() => {
    const resolved = this.variant() === 'auto' ? this.automaticVariant() : this.variant();
    return resolved === 'dark' ? '/novara-logo-dark.png' : '/novara-logo.png';
  });

  constructor() {
    const sync = (): void => {
      const selected = this.document.documentElement.dataset['theme'];
      this.automaticVariant.set(selected === 'light' ? 'light' : 'dark');
    };
    sync();
    const observer = new MutationObserver(sync);
    observer.observe(this.document.documentElement, {
      attributes: true,
      attributeFilter: ['data-theme'],
    });
    this.destroyRef.onDestroy(() => {
      observer.disconnect();
    });
  }
}
