/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.batch;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.core.lifecycle.BaseLifecycle;

/**
 * A lifecycle for loading KEW XML datasets.
 * This lifecycle will not be run (even if it is listed in the lifecycles list)
 * if the 'use.kewXmlmlDataLoaderLifecycle' configuration property is defined, and is
 * not 'true'.  If the property is omitted the lifecycle runs as normal.
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class KEWXmlDataLoaderLifecycle extends BaseLifecycle {
    private static final Logger LOG = Logger.getLogger(KEWXmlDataLoaderLifecycle.class);

    private String filename;

    /**
     * Configures the KEWXmlDataLoaderLifecycle to load the "default" data set from the classpath,
     * classpath:DefaultTestData.xml files
     */
    public KEWXmlDataLoaderLifecycle() {
        this("classpath:DefaultTestData.xml");
    }

    /**
     * Specifies the XML resource to load.  The resource path should be in Spring resource notation.
     * @param resource the XML resource to load
     */
    public KEWXmlDataLoaderLifecycle(String resource) {
        this.filename = resource;
    }

    public void start() throws Exception {
        String useKewXmlDataLoaderLifecycle = ConfigContext.getCurrentContextConfig().getProperty("use.kewXmlmlDataLoaderLifecycle");

        if (useKewXmlDataLoaderLifecycle != null && !Boolean.valueOf(useKewXmlDataLoaderLifecycle)) {
            LOG.debug("Skipping KEWXmlDataLoaderLifecycle due to property: use.kewXmlmlDataLoaderLifecycle=" + useKewXmlDataLoaderLifecycle);
            return;
        }

        LOG.info("################################");
        LOG.info("#");
        LOG.info("#  Begin Loading file '" + filename + "'");
        LOG.info("#");
        LOG.info("################################");
        loadData();
        super.start();
    }

    /**
     * Does the work of loading the data
     */
    protected void loadData() throws Exception {
        KEWXmlDataLoader.loadXmlResource(filename);
    }
}
