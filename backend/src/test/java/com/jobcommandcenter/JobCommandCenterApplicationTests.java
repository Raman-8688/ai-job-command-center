package com.jobcommandcenter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JobCommandCenterApplicationTests {

    @Test
    @DisplayName("Context loads successfully with test profile")
    void contextLoads() {
        // Verifies the Spring application context initializes without failure
    }
}
