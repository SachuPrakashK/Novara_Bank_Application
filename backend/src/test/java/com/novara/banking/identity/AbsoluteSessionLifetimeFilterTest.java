package com.novara.banking.identity;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

class AbsoluteSessionLifetimeFilterTest {
    @Test void expiredSessionIsInvalidatedAndCurrentRequestIsRejected() throws Exception {
        var request = new MockHttpServletRequest();
        var session = (MockHttpSession) request.getSession(true);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        new AbsoluteSessionLifetimeFilter(Duration.ofMillis(-1)).doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("SESSION_EXPIRED");
        assertThat(chain.getRequest()).isNull();
        assertThat(session.isInvalid()).isTrue();
    }
}
