export type BeneficiaryStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'RESTRICTED' | 'DEACTIVATED';
export type BeneficiaryTransferMethod = 'INTERNAL' | 'UPI' | 'IMPS' | 'NEFT' | 'RTGS';
export interface Beneficiary { readonly id:string; readonly name:string; readonly nickname?:string; readonly displayName:string; readonly bankName:string; readonly maskedAccount:string; readonly routingCode:string; readonly transferMethod:BeneficiaryTransferMethod; readonly status:BeneficiaryStatus; readonly favorite:boolean; readonly availableFrom?:string; readonly createdAt:string; readonly updatedAt:string; }
export interface BeneficiaryFilters { readonly query?:string; readonly status?:BeneficiaryStatus; readonly favorite?:boolean; }
export interface CreateBeneficiaryCommand { readonly name:string; readonly nickname?:string; readonly accountNumber:string; readonly routingCode:string; readonly bankName:string; readonly transferMethod:BeneficiaryTransferMethod; }
export interface UpdateBeneficiaryCommand { readonly name:string; readonly nickname?:string; }
