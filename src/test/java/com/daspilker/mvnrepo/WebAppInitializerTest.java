package com.daspilker.mvnrepo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

import static javax.servlet.DispatcherType.REQUEST;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.config.BeanIds.SPRING_SECURITY_FILTER_CHAIN;

@RunWith(MockitoJUnitRunner.class)
public class WebAppInitializerTest {
    @InjectMocks
    private WebAppInitializer webAppInitializer;

    @Mock
    private ServletContext servletContext;

    @Mock
    private ServletRegistration.Dynamic servletRegistrationDynamic;

    @Mock
    private FilterRegistration.Dynamic filterRegistrationDynamic;

    @Test
    public void testOnStartup() throws ServletException {
        when(servletContext.addServlet(eq("dispatcher"), any(DispatcherServlet.class))).thenReturn(servletRegistrationDynamic);
        when(servletContext.addFilter(eq(SPRING_SECURITY_FILTER_CHAIN), any(DelegatingFilterProxy.class))).thenReturn(filterRegistrationDynamic);

        webAppInitializer.onStartup(servletContext);

        verify(servletContext).addListener(any(ContextLoaderListener.class));
        verify(servletContext).addServlet(eq("dispatcher"), any(DispatcherServlet.class));
        verify(servletContext).addFilter(eq(SPRING_SECURITY_FILTER_CHAIN), any(DelegatingFilterProxy.class));
        verify(servletRegistrationDynamic).setLoadOnStartup(1);
        verify(servletRegistrationDynamic).addMapping("/*");
        verify(filterRegistrationDynamic).addMappingForServletNames(EnumSet.of(REQUEST), false, "dispatcher");
    }
}
