/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.kns.datadictionary.spring;

import java.util.List;

import org.kuali.rice.core.config.ConfigurationException;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.springframework.beans.factory.InitializingBean;

/**
 * Puts a data dictionary file location in the data dictionary
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class DataDictionaryLocationConfigurer implements InitializingBean {

	private List<String> dataDictionaryPackages;
	
	private DataDictionaryService dataDictionaryService;
	
	public DataDictionaryLocationConfigurer(DataDictionaryService dataDictionaryService){
		this.dataDictionaryService = dataDictionaryService;
	}
	
	public void afterPropertiesSet() throws Exception {
		if (dataDictionaryPackages == null || dataDictionaryPackages.isEmpty()) {
			throw new ConfigurationException("datatDictionaryPackages empty when initializing DataDictionaryLocation bean.");
		}
		if(dataDictionaryService!=null)
			dataDictionaryService.addDataDictionaryLocations(getDataDictionaryPackages());
		else
			KNSServiceLocator.getDataDictionaryService().addDataDictionaryLocations(getDataDictionaryPackages());
	}

	public List<String> getDataDictionaryPackages() {
		return dataDictionaryPackages;
	}

	public void setDataDictionaryPackages(List<String> dataDictionaryPackages) {
		this.dataDictionaryPackages = dataDictionaryPackages;
	}

}
