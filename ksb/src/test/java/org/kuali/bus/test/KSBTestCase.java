/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.kuali.bus.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.kuali.bus.services.KSBServiceLocator;
import org.kuali.rice.config.Config;
import org.kuali.rice.core.Core;
import org.kuali.rice.exceptions.RiceRuntimeException;
import org.kuali.rice.lifecycle.Lifecycle;
import org.kuali.rice.ojb.BaseOjbConfigurer;
import org.kuali.rice.resourceloader.GlobalResourceLoader;
import org.kuali.rice.resourceloader.ResourceLoader;
import org.kuali.rice.resourceloader.SpringResourceLoader;
import org.kuali.rice.test.RiceTestCase;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.springframework.context.ApplicationContext;

import edu.iu.uis.eden.messaging.bam.BAMService;
import edu.iu.uis.eden.messaging.bam.BAMTargetEntry;
import edu.iu.uis.eden.messaging.resourceloading.KSBResourceLoaderFactory;
import edu.iu.uis.eden.server.TestClient1;
import edu.iu.uis.eden.server.TestClient2;

public class KSBTestCase extends RiceTestCase {

    private TestClient1 testClient1;
    private TestClient2 testClient2;
    private ResourceLoader springContextResourceLoader;

    @Override
    public void setUp() throws Exception {
        // because we're stopping and starting so many times we need to clear
        // the core before
        // another set of RLs get put in the core. This is because we are
        // sometimes using
        // the GRL to fetch a specific servers spring file out for testing
        // purposes.
        Core.destroy();
        super.setUp();
        if (startClient1() || startClient2()) {
            ((Runnable) KSBResourceLoaderFactory.getRemoteResourceLocator()).run();
        }
        // new SQLDataLoader("classpath:db/DefaultTestData.sql", ";").runSql();
    }

    @Override
    protected List<String> getConfigLocations() {
        return Arrays.asList(new String[] { "classpath:META-INF/ksb-test-config.xml" });
    }

    @Override
    protected String getDerbySQLFileLocation() {
        return "classpath:db/derby/bus.sql";
    }

    @Override
    protected String getModuleName() {
        return "ksb";
    }

    @Override
    protected List<String> getTablesToClear() {
        List<String> tables = new ArrayList<String>();
        tables.add("EN_MSG_QUE_T");
        tables.add("EN_MSG_PAYLOAD_T");
        tables.add("EN_BAM_T");
        tables.add("EN_BAM_PARAM_T");
        tables.add("EN_SERVICE_DEF_DUEX_T");
        return tables;
    }

    @Override
    protected List<Lifecycle> getPerTestLifecycles() {
        List<Lifecycle> lifecycles = super.getSuiteLifecycles();
        if (this.disableJta()) {
            System.setProperty(BaseOjbConfigurer.OJB_PROPERTIES_PROP, "RiceNoJtaOJB.properties");
            this.springContextResourceLoader = new SpringResourceLoader(new QName("ksbtestharness"), "KSBTestHarnessNoJtaSpring.xml");
        } else {
            this.springContextResourceLoader = new SpringResourceLoader(new QName("ksbtestharness"), "KSBTestHarnessSpring.xml");
        }

        lifecycles.add(this.springContextResourceLoader);
        if (startClient1()) {
            this.testClient1 = new TestClient1();
            lifecycles.add(this.testClient1);
        }
        if (startClient2()) {
            this.testClient2 = new TestClient2();
            lifecycles.add(this.testClient2);
        }
        return lifecycles;
    }

    public boolean startClient1() {
        return false;
    }

    public boolean startClient2() {
        return false;
    }

    public TestClient1 getTestClient1() {
        return this.testClient1;
    }

    public TestClient2 getTestClient2() {
        return this.testClient2;
    }

    public static boolean verifyServiceCallsViaBam(QName serviceName, String methodName, boolean serverInvocation) throws Exception {
        BAMService bamService = KSBServiceLocator.getBAMService();
        List<BAMTargetEntry> bamCalls = null;
        if (methodName == null) {
            bamCalls = bamService.getCallsForService(serviceName);
        } else {
            bamCalls = bamService.getCallsForService(serviceName, methodName);
        }

        if (bamCalls.size() == 0) {
            return false;
        }
        for (BAMTargetEntry bamEntry : bamCalls) {
            if (bamEntry.getServerInvocation() && serverInvocation) {
                return true;
            } else if (!serverInvocation) {
                return true;
            }
        }
        return false;
    }

    public static Object getServiceFromWebAppResourceLoader(String serviceName) {
        Map<ClassLoader, Config> configs = Core.getCONFIGS();
        for (Map.Entry<ClassLoader, Config> configEntry : configs.entrySet()) {
            if (configEntry.getKey() instanceof WebAppClassLoader) {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                // to make GRL select services from correct classloader
                Thread.currentThread().setContextClassLoader(configEntry.getKey());
                try {
                    return GlobalResourceLoader.getService(serviceName);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        }
        throw new RiceRuntimeException("Couldn't find service " + serviceName + " in WebApp Resource Loader");
    }

    public static Object getServiceFromTestClient1SpringContext(String serviceName) {
        Map<ClassLoader, Config> configs = Core.getCONFIGS();
        for (Map.Entry<ClassLoader, Config> configEntry : configs.entrySet()) {
            if (configEntry.getKey() instanceof WebAppClassLoader) {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                // to make GRL select services from correct classloader
                Thread.currentThread().setContextClassLoader(configEntry.getKey());
                try {
                    // TestClient1SpringContext found in web.xml of TestClient1
                    ApplicationContext appContext = (ApplicationContext) Core.getCurrentContextConfig().getObject("TestClient1SpringContext");

                    return appContext.getBean(serviceName);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        }
        throw new RiceRuntimeException("Couldn't find service " + serviceName + " in TestClient1 Spring Context");
    }

    public ResourceLoader getSpringContextResourceLoader() {
        return this.springContextResourceLoader;
    }

    public void setSpringContextResourceLoader(ResourceLoader testHarnessResourceLoader) {
        this.springContextResourceLoader = testHarnessResourceLoader;
    }

    protected boolean disableJta() {
        return false;
    }
}