import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { STATEMENTS_API } from '../../core/statements/statements-api';
import type { ReceiptDetails, StatementAccount, StatementGeneration, StatementPage } from '../../core/statements/statement.models';
import { IconComponent } from '../../shared/ui/icon.component';
import { MoneyComponent } from '../../shared/ui/money.component';

@Component({selector:'app-statements',imports:[DatePipe,ReactiveFormsModule,IconComponent,MoneyComponent],templateUrl:'./statements.page.html',styleUrls:['./statements.page.scss','./statements-stabilization.scss'],changeDetection:ChangeDetectionStrategy.OnPush})
export class StatementsPage {
 private readonly api=inject(STATEMENTS_API);private readonly today=new Date();protected readonly accounts=signal<readonly StatementAccount[]>([]);protected readonly data=signal<StatementPage|null>(null);protected readonly loading=signal(true);protected readonly error=signal('');protected readonly generation=signal<StatementGeneration|null>(null);protected readonly receiptView=signal<ReceiptDetails|null>(null);
 protected readonly form=new FormGroup({accountId:new FormControl('',{nonNullable:true}),from:new FormControl(this.iso(new Date(this.today.getFullYear(),this.today.getMonth(),1)),{nonNullable:true}),to:new FormControl(this.iso(this.today),{nonNullable:true}),direction:new FormControl('',{nonNullable:true}),type:new FormControl('',{nonNullable:true}),search:new FormControl('',{nonNullable:true}),sort:new FormControl('newest',{nonNullable:true})});
 constructor(){void this.load();}
 protected async search(page=0){const v=this.form.getRawValue();if(!v.accountId)return;this.loading.set(true);this.error.set('');try{this.data.set(await firstValueFrom(this.api.transactions({...v,page,size:10})));}catch{this.error.set('Statement activity could not be loaded. Check the selected date range.');}finally{this.loading.set(false);}}
 protected clear(){this.form.patchValue({direction:'',type:'',search:'',sort:'newest'});void this.search();}
 protected preset(months:number){const to=new Date();const from=months===0?new Date(to.getFullYear(),to.getMonth(),1):new Date(to.getFullYear(),to.getMonth()-months+1,1);this.form.patchValue({from:this.iso(from),to:this.iso(to)});void this.search();}
 protected async generate(format:'pdf'|'csv'){this.error.set('');try{const v=this.form.getRawValue();const generated=await firstValueFrom(this.api.generate(v.accountId,v.from,v.to));this.generation.set(generated);this.save(await firstValueFrom(this.api.download(generated.id,format)),`Novara-Statement.${format}`);}catch{this.error.set('The statement download could not be completed.');}}
 protected async showReceipt(id:string){this.error.set('');try{this.receiptView.set(await firstValueFrom(this.api.receiptDetails(id)));}catch{this.error.set('A receipt is available only for an owned, posted transaction.');}}
 protected async receipt(id:string){try{this.save(await firstValueFrom(this.api.receipt(id)),'Novara-Receipt.pdf');}catch{this.error.set('The receipt download could not be completed.');}}
 private async load(){try{const accounts=await firstValueFrom(this.api.accounts());this.accounts.set(accounts);if(accounts[0]){this.form.controls.accountId.setValue(accounts[0].id);await this.search();}else this.loading.set(false);}catch{this.error.set('Statements are temporarily unavailable.');this.loading.set(false);}}
 private save(blob:Blob,name:string){const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download=name;a.click();URL.revokeObjectURL(url);}private iso(d:Date){return d.toISOString().slice(0,10);}
}
