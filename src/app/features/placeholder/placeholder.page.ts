import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { IconComponent } from '../../shared/ui/icon.component';
@Component({selector:'app-placeholder',imports:[RouterLink,IconComponent],template:`<section class="placeholder"><span><novara-icon name="building" label="Product area"/></span><p class="overline">FOUNDATION READY</p><h1>{{label}}</h1><p>This area has a defined route and product boundary. Its trusted banking workflow will be implemented in the next relevant milestone.</p><a routerLink="/dashboard">Return to dashboard</a></section>`,styleUrl:'../feature-page.scss',changeDetection:ChangeDetectionStrategy.OnPush})
export class PlaceholderPage{protected readonly label=inject(ActivatedRoute).snapshot.data['label'] as string;}
