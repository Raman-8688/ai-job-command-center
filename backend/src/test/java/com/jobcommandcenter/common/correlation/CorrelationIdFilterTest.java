package com.jobcommandcenter.common.correlation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Should generate UUID when no correlation ID header is provided")
    void shouldGenerateCorrelationIdWhenMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        String correlationId = response.getHeader(CorrelationIdHolder.CORRELATION_ID_HEADER);
        assertThat(correlationId).isNotNull().isNotBlank();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should reuse valid incoming correlation ID")
    void shouldPropagateValidIncomingCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdHolder.CORRELATION_ID_HEADER, "custom-test-id-12345");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader(CorrelationIdHolder.CORRELATION_ID_HEADER)).isEqualTo("custom-test-id-12345");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should generate fresh UUID when incoming correlation ID is invalid or malicious")
    void shouldGenerateFreshUuidWhenIncomingIsInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdHolder.CORRELATION_ID_HEADER, "<script>alert(1)</script>");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        String returnedId = response.getHeader(CorrelationIdHolder.CORRELATION_ID_HEADER);
        assertThat(returnedId).isNotEqualTo("<script>alert(1)</script>");
        assertThat(returnedId).hasSize(36); // standard UUID format
        verify(filterChain).doFilter(request, response);
    }
}
