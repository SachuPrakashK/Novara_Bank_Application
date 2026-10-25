# Statement PDF and CSV Export

PDF statements are rendered server-side from the stored accounting cutoff. They contain customer name, masked account, type, currency, period, cutoff, opening/money-in/money-out/closing summary, and the complete ordered transaction table. Table headers repeat across pages. The footer identifies the statement reference and sandbox status without regulatory claims.

CSV output is UTF-8 with BOM, stable ordering, fixed headers, quoted text cells, minor-unit numeric values, and currency. Text beginning with `=`, `+`, `-`, `@`, tab, or carriage return is prefixed with an apostrophe to prevent spreadsheet formula execution.

Generated records store a checksum and cutoff rather than duplicated financial rows. Historical output remains reproducible because posted journal data is immutable; reversals appear as later explicit entries.
