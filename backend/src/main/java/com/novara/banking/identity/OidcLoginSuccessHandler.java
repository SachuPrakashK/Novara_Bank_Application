package com.novara.banking.identity;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.novara.banking.audit.AuditService;
import org.slf4j.MDC;

@Component
public class OidcLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final String frontendUri;
    private final AuditService audit;
    private final CurrentIdentity identities;
    public OidcLoginSuccessHandler(@Value("${novara.security.frontend-uri}") String frontendUri, AuditService audit, CurrentIdentity identities) {
        this.frontendUri = frontendUri.replaceAll("/$", ""); this.audit = audit; this.identities = identities;
    }
    @Override public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {
        Object destination = request.getSession().getAttribute(AuthenticationController.RETURN_TO);
        request.getSession().removeAttribute(AuthenticationController.RETURN_TO);
        audit.record(identities.from(authentication).subject(), "OIDC_LOGIN", "SESSION", null, "SUCCESS", MDC.get("correlationId"), "{}");
        response.sendRedirect(frontendUri + (destination instanceof String path ? path : "/dashboard"));
    }
}
