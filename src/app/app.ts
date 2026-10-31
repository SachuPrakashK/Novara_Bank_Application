import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastHostComponent } from './shared/ui/toast-host.component';

@Component({
  imports: [RouterOutlet, ToastHostComponent],
  selector: 'app-root',
  template: '<router-outlet /><novara-toast-host />',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {}
