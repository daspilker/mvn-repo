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

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

import static javax.servlet.DispatcherType.REQUEST;
import static org.springframework.security.config.BeanIds.SPRING_SECURITY_FILTER_CHAIN;

public class WebAppInitializer implements WebApplicationInitializer {
    private static final String SERVLET_NAME_DISPATCHER = "dispatcher";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        XmlWebApplicationContext applicationContext = new XmlWebApplicationContext();

        ConfigurableWebEnvironment environment = applicationContext.getEnvironment();
        environment.setActiveProfiles(environment.getProperty("ENVIRONMENT", "development"));

        servletContext.addListener(new ContextLoaderListener(applicationContext));

        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet(SERVLET_NAME_DISPATCHER, dispatcherServlet);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/*");

        DelegatingFilterProxy filterProxy = new DelegatingFilterProxy(SPRING_SECURITY_FILTER_CHAIN, applicationContext);
        FilterRegistration.Dynamic securityFilter = servletContext.addFilter(SPRING_SECURITY_FILTER_CHAIN, filterProxy);
        securityFilter.addMappingForServletNames(EnumSet.of(REQUEST), false, SERVLET_NAME_DISPATCHER);
    }
}
