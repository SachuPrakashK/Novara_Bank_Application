import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type NovaraIcon='arrow-up-right'|'arrow-left'|'arrow-right'|'panel-left-close'|'panel-left-open'|'users'|'calendar'|'repeat'|'history'|'shield'|'receipt'|'wallet'|'check'|'alert'|'search'|'limits'|'home'|'card'|'deposit'|'loan'|'chart'|'help'|'logout'|'menu'|'close'|'moon'|'sun'|'bell'|'star'|'building'|'lock'|'eye'|'eye-off'|'plus'|'qr';
@Component({selector:'novara-icon',template:`<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" [attr.aria-hidden]="label()?null:'true'" [attr.aria-label]="label()||null" role="img">
@switch(name()){
 @case('arrow-up-right'){<path d="M7 17 17 7M7 7h10v10"/>}
 @case('users'){<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>}
 @case('calendar'){<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 11h18"/>}
 @case('repeat'){<path d="m17 1 4 4-4 4M3 11V9a4 4 0 0 1 4-4h14M7 23l-4-4 4-4M21 13v2a4 4 0 0 1-4 4H3"/>}
 @case('history'){<path d="M3 12a9 9 0 1 0 3-6.7L3 8M3 3v5h5M12 7v5l3 2"/>}
 @case('shield'){<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"/><path d="m9 12 2 2 4-4"/>}
 @case('receipt'){<path d="M6 2h12v20l-3-2-3 2-3-2-3 2V2Z"/><path d="M9 7h6M9 11h6M9 15h3"/>}
 @case('wallet'){<path d="M20 7V5a2 2 0 0 0-2-2H5a3 3 0 0 0 0 6h15v12H5a3 3 0 0 1-3-3V6"/><path d="M16 13h2"/>}
 @case('check'){<path d="m5 12 4 4L19 6"/>}
 @case('alert'){<path d="M12 9v4M12 17h.01"/><path d="M10.3 3.7 2.6 17a2 2 0 0 0 1.7 3h15.4a2 2 0 0 0 1.7-3L13.7 3.7a2 2 0 0 0-3.4 0Z"/>}
 @case('search'){<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>}
 @case('limits'){<path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/>}
 @case('home'){<path d="m3 11 9-8 9 8"/><path d="M5 10v10h14V10M9 20v-6h6v6"/>}
 @case('card'){<rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20M6 15h2"/>}
 @case('deposit'){<path d="M3 10h18M5 10v8M9 10v8M15 10v8M19 10v8M2 21h20M12 3 2 8h20L12 3Z"/>}
 @case('loan'){<circle cx="12" cy="12" r="9"/><path d="M16 8h-6a2 2 0 0 0 0 4h4a2 2 0 0 1 0 4H8M12 6v12"/>}
 @case('chart'){<path d="M3 3v18h18M7 16l4-5 3 3 5-7"/>}
 @case('help'){<circle cx="12" cy="12" r="9"/><path d="M9.5 9a2.5 2.5 0 1 1 3.5 2.3c-.7.3-1 1-1 1.7M12 17h.01"/>}
 @case('logout'){<path d="M10 17l5-5-5-5M15 12H3M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>}
 @case('menu'){<path d="M4 6h16M4 12h16M4 18h16"/>}
 @case('close'){<path d="m6 6 12 12M18 6 6 18"/>}
 @case('moon'){<path d="M21 12.8A9 9 0 1 1 11.2 3 7 7 0 0 0 21 12.8Z"/>}
 @case('sun'){<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>}
 @case('bell'){<path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/>}
 @case('star'){<path d="m12 3 2.8 5.7 6.2.9-4.5 4.4 1.1 6.2-5.6-3-5.6 3 1.1-6.2L3 9.6l6.2-.9L12 3Z"/>}
 @case('building'){<path d="M3 21h18M6 21V8h12v13M9 12h2M13 12h2M9 16h2M13 16h2M8 8V4h8v4"/>}
 @case('lock'){<rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/>}
 @case('arrow-left'){<path d="m15 18-6-6 6-6"/>}
 @case('arrow-right'){<path d="m9 18 6-6-6-6"/>}
 @case('panel-left-close'){<rect x="3" y="4" width="18" height="16" rx="2"/><path d="M9 4v16M16 9l-3 3 3 3"/>}
 @case('panel-left-open'){<rect x="3" y="4" width="18" height="16" rx="2"/><path d="M9 4v16m4-11 3 3-3 3"/>}
 @case('eye'){<path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/>}
 @case('eye-off'){<path d="m3 3 18 18M10.6 10.6a2 2 0 0 0 2.8 2.8M9.9 4.2A10.8 10.8 0 0 1 12 5c6.5 0 10 7 10 7a16 16 0 0 1-2.1 3.2M6.6 6.6C3.6 8.5 2 12 2 12s3.5 7 10 7a10 10 0 0 0 3.4-.6"/>}
 @case('plus'){<path d="M12 5v14M5 12h14"/>}
 @case('qr'){<rect x="3" y="3" width="6" height="6"/><rect x="15" y="3" width="6" height="6"/><rect x="3" y="15" width="6" height="6"/><path d="M15 15h2v2h-2zM19 15h2v6h-6v-2"/>}
}</svg>`,styles:[`:host{display:inline-grid;width:1.25rem;height:1.25rem}svg{width:100%;height:100%}`],changeDetection:ChangeDetectionStrategy.OnPush})
export class IconComponent{readonly name=input.required<NovaraIcon>();readonly label=input<string>();}
