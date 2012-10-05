/*
   Copyright 2012 Daniel A. Spilker

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

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

    @Test
    public void testOnStartupWithProfile() throws ServletException {

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
