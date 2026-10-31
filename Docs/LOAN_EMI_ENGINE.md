# Loan EMI Engine

The engine uses fixed monthly periodic interest and a reducing principal balance. For a non-zero monthly rate `r`, principal `P`, and installment count `n`:

`EMI = P × r × (1 + r)^n / ((1 + r)^n - 1)`

The monthly rate is annual basis points divided by 10,000 and 12. Intermediate arithmetic uses 34-digit decimal precision. Monetary output is rounded to integer minor units with `HALF_EVEN`. Zero-rate products divide principal by installment count.

For each period, interest is rounded from opening principal. Scheduled principal is EMI less interest. The final installment replaces scheduled principal with the exact remaining principal, guaranteeing a zero closing balance. Totals are the sum of persisted installment values, not a separately rounded approximation.

No day-count convention, holiday adjustment, variable-rate reset, fee capitalization, penalty, or tax behavior is implemented.
