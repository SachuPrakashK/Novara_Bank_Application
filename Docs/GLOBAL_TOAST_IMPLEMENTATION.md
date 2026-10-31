# Global Toast Implementation

`ErrorNotificationService` remains the safe compatibility entry point used by the HTTP interceptor and now exposes:

- `success`, `error`, `warning`, and `info` methods;
- optional titles, durations, and callable actions;
- duplicate suppression by tone/title/message;
- a four-message visible stack;
- persistent errors by default;
- automatic cleanup for transient messages;
- manual dismissal and dismiss-all.

`ToastHostComponent` is mounted beside the root router outlet, so notifications work in authentication, customer, operations, and lazy-loaded views. Errors use `role="alert"`; other tones use polite status announcements. The host is responsive, does not take focus, and honors reduced motion.

Raw backend content is not rendered. Existing API normalization continues to supply safe messages. An action is rendered only when an actual callback is provided, avoiding a non-functional “Try again” control.
