# Loan Repayment Schedule

Quote creation persists a one-to-one ordered schedule containing sequence, due date, opening principal, principal, interest, fee, total due, and closing principal. The first due date is one calendar month after quote creation; subsequent dates use calendar-month increments. No business-day adjustment is applied.

Schedule rows are `ILLUSTRATIVE`. They are not receivables and do not authorize collection. The current customer API exposes only schedules belonging to that customer. Repayment execution, allocation, missed-payment handling, arrears, and schedule restructuring are policy-blocked.
