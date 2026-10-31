# Deposit Interest Engine

Money remains integer minor units at API/storage boundaries. Calculations use `BigDecimal` with `HALF_EVEN` rounding. Enabled methods are simple interest and monthly compounding. FD projection compounds the principal for the accepted contract months. RD projection compounds every installment for its own remaining periods; accrued-cash-flow support uses actual posting dates and excludes future cash flows.

Projections are estimates. V7 records `ACTUAL_365` policy metadata, but production calendar/day-boundary behavior requires an approved banking calendar and product specification.
