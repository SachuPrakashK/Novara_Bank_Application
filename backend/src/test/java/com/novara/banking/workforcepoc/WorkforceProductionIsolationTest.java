package com.novara.banking.workforcepoc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class WorkforceProductionIsolationTest {
    @Test void workforcePocHasNoProductionClassOrPackagedRealmResource() {
        assertThat(getClass().getClassLoader().getResource("novara-workforce-test-realm.json")).isNull();
        assertThatThrownBy(() -> Class.forName("com.novara.banking.workforce.WorkforceSecurityConfig"))
            .isInstanceOf(ClassNotFoundException.class);
    }
}
