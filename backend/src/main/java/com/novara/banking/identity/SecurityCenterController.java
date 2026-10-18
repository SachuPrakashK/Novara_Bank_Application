package com.novara.banking.identity;

import java.time.Instant;
import java.util.List;

import com.novara.banking.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.Authentication;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/security")
public class SecurityCenterController {
    private final FindByIndexNameSessionRepository<?> sessions;
    private final CurrentIdentity identities;
    private final AuditService audit;
    private final JdbcClient jdbc;
    public SecurityCenterController(FindByIndexNameSessionRepository<?> sessions, CurrentIdentity identities, AuditService audit, JdbcClient jdbc) {
        this.sessions = sessions; this.identities = identities; this.audit = audit; this.jdbc = jdbc;
    }

    @GetMapping("/sessions")
    List<SessionView> sessions(Authentication authentication, HttpServletRequest request) {
        return sessions.findByPrincipalName(authentication.getName()).values().stream().map(value -> (Session) value)
            .map(session -> new SessionView(session.getId(), session.getCreationTime(), session.getLastAccessedTime(),
                session.getLastAccessedTime().plus(session.getMaxInactiveInterval()), session.getId().equals(request.getSession().getId())))
            .sorted((left, right) -> right.lastAccessedAt().compareTo(left.lastAccessedAt())).toList();
    }

    @DeleteMapping("/sessions/{sessionId}")
    void terminate(@PathVariable String sessionId, Authentication authentication, HttpServletRequest request) {
        sessions.findByPrincipalName(authentication.getName()).values().stream().map(value -> (Session) value)
            .filter(session -> session.getId().equals(sessionId) && !session.getId().equals(request.getSession().getId()))
            .findFirst().ifPresent(session -> {
                sessions.deleteById(session.getId());
                audit.record(identities.subject(authentication), "SESSION_TERMINATED", "SESSION", null, "SUCCESS", MDC.get("correlationId"), "{}");
            });
    }

    @GetMapping("/events")
    List<EventView> events(Authentication authentication) {
        return jdbc.sql("SELECT action,outcome,occurred_at FROM audit_events WHERE actor_subject=:subject AND action IN ('SESSION_TERMINATED','OIDC_LOGIN') ORDER BY occurred_at DESC LIMIT 20")
            .param("subject", identities.subject(authentication))
            .query((rs, row) -> new EventView(rs.getString(1), rs.getString(2), rs.getTimestamp(3).toInstant())).list();
    }

    record SessionView(String id, Instant createdAt, Instant lastAccessedAt, Instant expiresAt, boolean current) {}
    record EventView(String action, String outcome, Instant occurredAt) {}
}
