import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
@Component({selector:'app-placeholder',imports:[RouterLink],template:`<section class="placeholder"><span>◇</span><p class="overline">FOUNDATION READY</p><h1>{{label}}</h1><p>This area has a defined route and product boundary. Its trusted banking workflow will be implemented in the next relevant milestone.</p><a routerLink="/dashboard">Return to dashboard</a></section>`,styleUrl:'../feature-page.scss',changeDetection:ChangeDetectionStrategy.OnPush})
export class PlaceholderPage{protected readonly label=inject(ActivatedRoute).snapshot.data['label'] as string;}
