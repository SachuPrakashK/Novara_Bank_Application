import type { Routes } from '@angular/router';
import { authGuard, guestGuard, mfaChallengeGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'login', canActivate: [guestGuard], loadComponent: () => import('./features/auth/login.page').then((m) => m.LoginPage), title: 'Sign in | Novara Bank' },
  { path: 'auth/mfa', canActivate: [mfaChallengeGuard], loadComponent: () => import('./features/auth/mfa.page').then((m) => m.MfaPage), title: 'Verify your identity | Novara Bank' },
  {
    path: '', canActivate: [authGuard],
    loadComponent: () => import('./layout/app-shell.component').then((m) => m.AppShellComponent),
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.page').then((m) => m.DashboardPage), title: 'Dashboard | Novara Bank' },
      { path: 'accounts', loadComponent: () => import('./features/accounts/accounts.page').then((m) => m.AccountsPage), title: 'Accounts | Novara Bank' },
      { path: 'accounts/:accountId', loadComponent: () => import('./features/accounts/account-details.page').then((m) => m.AccountDetailsPage), title: 'Account details | Novara Bank' },
      { path: 'transactions', loadComponent: () => import('./features/transactions/transactions.page').then((m) => m.TransactionsPage), title: 'Transactions | Novara Bank' },
      { path: 'transactions/:transactionId', loadComponent: () => import('./features/transactions/transaction-details.page').then((m) => m.TransactionDetailsPage), title: 'Transaction details | Novara Bank' },
      { path: 'payments', loadComponent: () => import('./features/payments/payments-overview.page').then((m) => m.PaymentsOverviewPage), title: 'Payments | Novara Bank' },
      { path: 'payments/transfer', loadComponent: () => import('./features/payments/transfer.page').then((m) => m.TransferPage), title: 'New transfer | Novara Bank' },
      { path: 'payments/review', loadComponent: () => import('./features/payments/payment-review.page').then((m) => m.PaymentReviewPage), title: 'Review transfer | Novara Bank' },
      { path: 'payments/result', loadComponent: () => import('./features/payments/payment-result.page').then((m) => m.PaymentResultPage), title: 'Transfer status | Novara Bank' },
      { path: 'payments/history', loadComponent: () => import('./features/payments/payment-history.page').then((m) => m.PaymentHistoryPage), title: 'Payment history | Novara Bank' },
      { path: 'payments/schedules', loadComponent: () => import('./features/payments/payment-schedules.page').then((m) => m.PaymentSchedulesPage), title: 'Scheduled payments | Novara Bank' },
      { path: 'payments/schedules/new', loadComponent: () => import('./features/payments/create-schedule.page').then((m) => m.CreateSchedulePage), title: 'Create payment schedule | Novara Bank' },
      { path: 'payments/limits', loadComponent: () => import('./features/payments/payment-limits.page').then((m) => m.PaymentLimitsPage), title: 'Transaction limits | Novara Bank' },
      { path: 'payments/:paymentId', loadComponent: () => import('./features/payments/payment-detail.page').then((m) => m.PaymentDetailPage), title: 'Payment details | Novara Bank' },
      { path: 'beneficiaries', loadComponent: () => import('./features/beneficiaries/beneficiaries.page').then((m) => m.BeneficiariesPage), title: 'Beneficiaries | Novara Bank' },
      { path: 'security', loadComponent: () => import('./features/security/security-center.page').then((m) => m.SecurityCenterPage), title: 'Security center | Novara Bank' },
      { path: 'profile', loadComponent: () => import('./features/self-service/profile.page').then((m) => m.ProfilePage), title: 'Profile | Novara Bank' },
      { path: 'account-services', loadComponent: () => import('./features/self-service/account-services.page').then((m) => m.AccountServicesPage), title: 'Account services | Novara Bank' },
      { path: 'notifications', loadComponent: () => import('./features/self-service/notifications.page').then((m) => m.NotificationsPage), title: 'Notifications | Novara Bank' },
      { path: 'preferences', loadComponent: () => import('./features/self-service/preferences.page').then((m) => m.PreferencesPage), title: 'Preferences | Novara Bank' },
      { path: 'cards', loadComponent: () => import('./features/cards/cards.page').then((m) => m.CardsPage), title: 'Cards | Novara Bank' },
      { path: 'cards/:cardId', loadComponent: () => import('./features/cards/card-details.page').then((m) => m.CardDetailsPage), title: 'Card details | Novara Bank' },
      { path: 'deposits', loadComponent: () => import('./features/deposits/deposits.page').then((m) => m.DepositsPage), title: 'Deposits | Novara Bank' },
      { path: 'loans', loadComponent: () => import('./features/loans/loans.page').then((m) => m.LoansPage), title: 'Loans | Novara Bank' },
      { path: 'statements', loadComponent: () => import('./features/statements/statements.page').then((m) => m.StatementsPage), title: 'Statements & Receipts | Novara Bank' },
      ...['investments', 'support'].map((path) => ({
        path, loadComponent: () => import('./features/placeholder/placeholder.page').then((m) => m.PlaceholderPage), data: { label: path[0].toUpperCase() + path.slice(1) },
      })),
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '' },
];
