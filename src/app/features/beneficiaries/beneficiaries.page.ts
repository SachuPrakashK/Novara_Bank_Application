import { ChangeDetectionStrategy,Component,computed,inject,signal } from '@angular/core';
import { FormControl,FormGroup,ReactiveFormsModule,Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { BENEFICIARIES_API } from '../../core/beneficiaries/beneficiaries-api';
import type { Beneficiary,BeneficiaryStatus } from '../../core/beneficiaries/beneficiary.models';

@Component({selector:'app-beneficiaries',imports:[ReactiveFormsModule,RouterLink],templateUrl:'./beneficiaries.page.html',styleUrl:'./beneficiaries.page.scss',changeDetection:ChangeDetectionStrategy.OnPush})
export class BeneficiariesPage {
  private readonly api=inject(BENEFICIARIES_API);
  protected readonly items=signal<readonly Beneficiary[]>([]);protected readonly query=signal('');protected readonly status=signal<BeneficiaryStatus|''>('');
  protected readonly adding=signal(false);protected readonly editing=signal<string|null>(null);protected readonly selected=signal<Beneficiary|null>(null);
  protected readonly loading=signal(true);protected readonly saving=signal(false);protected readonly error=signal('');protected readonly notice=signal('');
  protected readonly filtered=computed(()=>{const query=this.query().trim().toLowerCase(),status=this.status();return this.items().filter(item=>(!query||`${item.name} ${item.nickname??''} ${item.bankName}`.toLowerCase().includes(query))&&(!status||item.status===status));});
  protected readonly form=new FormGroup({name:new FormControl('',{nonNullable:true,validators:[Validators.required,Validators.maxLength(80)]}),nickname:new FormControl('',{nonNullable:true,validators:[Validators.maxLength(40)]}),bankName:new FormControl('',{nonNullable:true,validators:[Validators.required,Validators.maxLength(100)]}),accountNumber:new FormControl('',{nonNullable:true,validators:[Validators.required,Validators.pattern(/^\d{8,18}$/)]}),confirmAccountNumber:new FormControl('',{nonNullable:true,validators:[Validators.required]}),routingCode:new FormControl('',{nonNullable:true,validators:[Validators.required,Validators.pattern(/^[A-Za-z0-9-]{5,20}$/)]}),transferMethod:new FormControl<'INTERNAL'|'IMPS'|'NEFT'|'RTGS'>('INTERNAL',{nonNullable:true})});
  protected readonly editForm=new FormGroup({name:new FormControl('',{nonNullable:true,validators:[Validators.required,Validators.maxLength(80)]}),nickname:new FormControl('',{nonNullable:true,validators:[Validators.maxLength(40)]})});
  constructor(){void this.load();}
  protected statusLabel(value:BeneficiaryStatus):string{return value.toLowerCase().replaceAll('_',' ');}
  protected isPayable(item:Beneficiary):boolean{return item.status==='ACTIVE'&&(!item.availableFrom||new Date(item.availableFrom).getTime()<=Date.now());}
  protected displayStatus(item:Beneficiary):string{return item.status==='ACTIVE'&&!this.isPayable(item)?'cooling period':this.statusLabel(item.status);}
  protected toggleAdd():void{this.adding.update(value=>!value);this.error.set('');this.notice.set('');}
  protected async add():Promise<void>{if(this.form.invalid){this.form.markAllAsTouched();this.error.set('Complete all required details using valid banking identifiers.');return;}const value=this.form.getRawValue();if(value.accountNumber!==value.confirmAccountNumber){this.error.set('The account numbers do not match.');return;}this.saving.set(true);this.error.set('');try{const created=await firstValueFrom(this.api.create({...value},crypto.randomUUID().replaceAll('-','_')));this.items.update(items=>[created,...items]);this.form.reset({transferMethod:'INTERNAL'});this.adding.set(false);this.notice.set('Beneficiary submitted for independent verification. Payments remain unavailable until verification and cooling controls complete.');}catch{this.error.set('We could not add this beneficiary. The destination may already exist, or the service may be unavailable.');}finally{this.saving.set(false);}}
  protected beginEdit(item:Beneficiary):void{this.editing.set(item.id);this.editForm.setValue({name:item.name,nickname:item.nickname??''});}
  protected async saveEdit(item:Beneficiary):Promise<void>{if(this.editForm.invalid)return;this.saving.set(true);try{const updated=await firstValueFrom(this.api.update(item.id,this.editForm.getRawValue()));this.replace(updated);this.editing.set(null);this.notice.set('Beneficiary display details updated.');}catch{this.error.set('The beneficiary could not be updated.');}finally{this.saving.set(false);}}
  protected async toggleFavorite(item:Beneficiary):Promise<void>{try{this.replace(await firstValueFrom(this.api.setFavorite(item.id,!item.favorite)));}catch{this.error.set('Favorite status could not be updated.');}}
  protected async deactivate(item:Beneficiary):Promise<void>{if(!confirm(`Deactivate ${item.displayName}? Future payments to this beneficiary will be blocked.`))return;this.saving.set(true);try{await firstValueFrom(this.api.deactivate(item.id));this.items.update(items=>items.map(value=>value.id===item.id?{...value,status:'DEACTIVATED',favorite:false}:value));this.selected.set(null);this.notice.set('Beneficiary deactivated.');}catch{this.error.set('Recent MFA is required to deactivate this beneficiary. Sign in again with MFA and retry.');}finally{this.saving.set(false);}}
  private replace(updated:Beneficiary):void{this.items.update(items=>items.map(item=>item.id===updated.id?updated:item));if(this.selected()?.id===updated.id)this.selected.set(updated);}
  private async load():Promise<void>{this.loading.set(true);try{this.items.set(await firstValueFrom(this.api.list()));}catch{this.error.set('Beneficiaries are temporarily unavailable.');}finally{this.loading.set(false);}}
}
