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
package org.kuali.rice.kew.plugin;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.core.resourceloader.ResourceLoader;
import org.kuali.rice.kew.exception.InvalidXmlException;


/**
 * A {@link PluginLoader} which creates a {@link Plugin} with the given ClassLoader.
 * 
 * <p>This PluginLoader is used in the cases where the Plugin's ClassLoader was created 
 * by the calling code and doesn't need to be created by the loader.
 * 
 * @see Plugin
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class ClassLoaderPluginLoader implements PluginLoader {
	
	private String pluginConfigPath;
	private ClassLoader classLoader;
	
	public ClassLoaderPluginLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	public String getPluginName() {
	    return ResourceLoader.EMBEDDED_PLUGIN;
	}
	
	public Plugin load() throws Exception {
		//for now default the embedded plugin to the M.E. of the current context
		QName name = new QName(ConfigContext.getCurrentContextConfig().getServiceNamespace(), ResourceLoader.EMBEDDED_PLUGIN);
		Plugin plugin = new Plugin(name, loadPluginConfig(pluginConfigPath), classLoader);
		plugin.bindThread();
		try {
			PluginUtils.installResourceLoader(plugin);
			PluginUtils.installPluginListeners(plugin);
		} finally {
			plugin.unbindThread();
		}
		return plugin;
	}

	public void setPluginConfigPath(String pluginConfigPath) {
		this.pluginConfigPath = pluginConfigPath;
	}
	
	public boolean isRemoved() {
		return false;
	}
	
	public boolean isModified() {
		return false;
	}

    private PluginConfig loadPluginConfig(String pluginConfigPath) {
        PluginConfigParser parser = new PluginConfigParser();
        try {
            PluginConfig pluginConfig  = parser.parse(classLoader.getResource(pluginConfigPath), ConfigContext.getCurrentContextConfig());
            pluginConfig.parseConfig();
            return pluginConfig;
        } catch (FileNotFoundException e) {
            throw new PluginException("Could not locate the plugin config file at path " + pluginConfigPath, e);
        } catch (IOException ioe) {
            throw new PluginException("Could not read the plugin config file", ioe);
        } catch (InvalidXmlException ixe) {
            throw new PluginException("Could not parse the plugin config file", ixe);
        }
    }
	
}
