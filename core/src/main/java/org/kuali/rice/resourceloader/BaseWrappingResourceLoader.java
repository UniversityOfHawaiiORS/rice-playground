/*
 * Copyright 2005-2006 The Kuali Foundation.
 *
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.resourceloader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.RiceConstants;
import org.kuali.rice.core.Core;
import org.kuali.rice.definition.ObjectDefinition;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * A BaseResourceLoader implementation which wraps services with a Proxy that
 * switches the current context ClassLoader of the Thread.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class BaseWrappingResourceLoader extends BaseResourceLoader {

	private static final String[] PACKAGES_TO_FILTER = new String[] { "org.springframework" };
	private Set<QName> servicesToCache = new HashSet<QName>();
	private Map<QName, Object> serviceCache = Collections.synchronizedMap(new HashMap<QName, Object>());

	public BaseWrappingResourceLoader(QName name, ClassLoader classLoader, ServiceLocator serviceLocator) {
		super(name, classLoader, serviceLocator);
	}

	public BaseWrappingResourceLoader(QName name, ClassLoader classLoader) {
		super(name, classLoader);
	}

	public BaseWrappingResourceLoader(QName name, ServiceLocator serviceLocator) {
		super(name, serviceLocator);
	}

	public BaseWrappingResourceLoader(QName name) {
		super(name);
	}



	@Override
	public void start() throws Exception {
	    String servicesToCacheFromConfig = Core.getCurrentContextConfig().getProperty(RiceConstants.SERVICES_TO_CACHE);
	    if (!StringUtils.isEmpty(servicesToCacheFromConfig)) {
		String[] services = servicesToCacheFromConfig.split(",");
		for (String serviceName : services) {
		    serviceName = serviceName.trim();
		    try {
			servicesToCache.add(QName.valueOf(serviceName));
			LOG.info("Adding service " + serviceName + " to service cache.");
		    } catch (IllegalArgumentException e) {
			LOG.error("Failed to parse serviceName into QName from property " + RiceConstants.SERVICES_TO_CACHE +".  Service name given was: " + serviceName);
		    }
		}
	    }
	    super.start();
	}

	@Override
	public Object getService(QName serviceName) {
	    Object service = serviceCache.get(serviceName);
	    if (service != null) {
		if (LOG.isDebugEnabled()) {
		    LOG.debug("Service with QName " + serviceName + " was retrieved from the service cache.");
		}
		return service;
	    }
	    return super.getService(serviceName);
	}

	protected Object postProcessService(QName serviceName, Object service) {
		if (service != null && shouldWrapService(serviceName, service)) {
			service = ContextClassLoaderProxy.wrap(service, getInterfacesToProxy(service), getClassLoader());
		}
		cacheService(serviceName, service);
		return service;
	}

	protected Object postProcessObject(ObjectDefinition definition, Object object) {
		if (object != null && shouldWrapObject(definition, object)) {
			return ContextClassLoaderProxy.wrap(object, getInterfacesToProxy(object), getClassLoader());
		}
		return object;
	}

	protected void cacheService(QName serviceName, Object service) {
	    if (shouldCacheService(serviceName, service)) {
		LOG.debug("Adding service " + serviceName + " to the service cache.");
		serviceCache.put(serviceName, service);
	    }
	}

	protected Class[] getInterfacesToProxy(Object object) {
		List interfaces = ClassUtils.getAllInterfaces(object.getClass());
		for (Iterator iterator = interfaces.iterator(); iterator.hasNext();) {
			Class objectInterface = (Class) iterator.next();
			for (String packageNames : getPackageNamesToFilter()) {
				if (objectInterface.getName().startsWith(packageNames)) {
					iterator.remove();
				}
			}
		}
		Class[] interfaceArray = new Class[interfaces.size()];
		return (Class[]) interfaces.toArray(interfaceArray);
	}

	protected String[] getPackageNamesToFilter() {
		return PACKAGES_TO_FILTER;
	}

	protected boolean shouldWrapService(QName serviceName, Object service) {
		return true;
	}

	protected boolean shouldCacheService(QName serviceName, Object service) {
	    return servicesToCache.contains(serviceName);
	}

	protected boolean shouldWrapObject(ObjectDefinition definition, Object object) {
		return true;
	}

}