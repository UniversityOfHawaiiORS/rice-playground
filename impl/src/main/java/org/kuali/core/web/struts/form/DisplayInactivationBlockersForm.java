/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.core.web.struts.form;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.KNSServiceLocator;
import org.kuali.rice.core.service.EncryptionService;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.service.DataDictionaryService;

/**
 * This is a description of what this class does - wliang don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class DisplayInactivationBlockersForm extends KualiForm {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisplayInactivationBlockersForm.class);

	private String businessObjectClassName;
	private Map<String, String> primaryKeyFieldValues;
	private Map<String, List<String>> blockingValues;
	
	@Override
	public void populate(HttpServletRequest request) {
		super.populate(request);
		primaryKeyFieldValues = new HashMap<String, String>();

		if (StringUtils.isBlank(businessObjectClassName)) {
			throw new IllegalArgumentException("BO Class Name missing.");
		}
		
		Class businessObjectClass = null;
		try {
			businessObjectClass = Class.forName(businessObjectClassName);
		} catch (ClassNotFoundException e) {
			LOG.error("Unable to find class: " + businessObjectClassName, e);
			throw new IllegalArgumentException("Unable to find class: " + businessObjectClassName, e);
		}
		
		if (!BusinessObject.class.isAssignableFrom(businessObjectClass)) {
			LOG.error("BO Class is not a BusinessObject: " + businessObjectClassName);
			throw new IllegalArgumentException("BO Class is not a BusinessObject: " + businessObjectClassName);
		}
		
		DataDictionaryService dataDictionaryService = KNSServiceLocator.getDataDictionaryService();
		EncryptionService encryptionService = KNSServiceLocator.getEncryptionService();
		
		List primaryKeyFieldNames = KNSServiceLocator.getPersistenceStructureService().getPrimaryKeys(businessObjectClass);
		for (Iterator i = primaryKeyFieldNames.iterator(); i.hasNext();) {
			String primaryKeyFieldName = (String) i.next();
			
			String primaryKeyFieldValue = request.getParameter(primaryKeyFieldName);
			if (StringUtils.isBlank(primaryKeyFieldValue)) {
				LOG.error("Missing primary key value for: " + primaryKeyFieldName);
				throw new IllegalArgumentException("Missing primary key value for: " + primaryKeyFieldName);
			}
			
            // check if field is a secure
            String displayWorkgroup = dataDictionaryService.getAttributeDisplayWorkgroup(businessObjectClass, primaryKeyFieldValue);
            if (StringUtils.isNotBlank(displayWorkgroup)) {
                try {
                	primaryKeyFieldValue = encryptionService.decrypt(primaryKeyFieldValue);
                }
                catch (GeneralSecurityException e) {
                    LOG.error("Unable to decrypt secure field for BO " + businessObjectClassName + " field " + primaryKeyFieldName, e);
                    throw new RuntimeException("Unable to decrypt secure field for BO " + businessObjectClassName + " field " + primaryKeyFieldName, e);
                }
            }
            
			primaryKeyFieldValues.put(primaryKeyFieldName, primaryKeyFieldValue);
		}
	}

	public String getBusinessObjectClassName() {
		return this.businessObjectClassName;
	}

	public void setBusinessObjectClassName(String businessObjectClassName) {
		this.businessObjectClassName = businessObjectClassName;
	}

	public Map<String, String> getPrimaryKeyFieldValues() {
		return this.primaryKeyFieldValues;
	}

	public void setPrimaryKeyFieldValues(Map<String, String> primaryKeyFieldValues) {
		this.primaryKeyFieldValues = primaryKeyFieldValues;
	}

	public Map<String, List<String>> getBlockingValues() {
		return this.blockingValues;
	}

	public void setBlockingValues(Map<String, List<String>> blockingValues) {
		this.blockingValues = blockingValues;
	}	
}
