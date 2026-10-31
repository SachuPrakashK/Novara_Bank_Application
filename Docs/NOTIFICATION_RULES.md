# Notification Rules

In-app delivery is implemented. Email, SMS, push and WhatsApp are provider-blocked and not presented as active.

Transfer notices originate only from committed transfer outbox events. Service-request notices commit with the request. Source-event uniqueness prevents duplicate notices on retry. Messages contain no full account number or sensitive payload. Mandatory security alerts are displayed as non-disableable; optional category preferences do not suppress legally required communications.

Phase H does not yet project deposits, statements or loans because those modules do not emit approved durable events. Preferences for these categories are stored for future producers.

