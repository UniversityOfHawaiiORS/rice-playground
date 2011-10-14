/*
 * Copyright 2006-2011 The Kuali Foundation
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

package org.kuali.rice.krad.service.impl;

import org.kuali.rice.core.api.component.Component;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.kns.lookup.LookupUtils;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.datadictionary.AttributeDefinition;
import org.kuali.rice.krad.datadictionary.BusinessObjectEntry;
import org.kuali.rice.krad.datadictionary.DocumentEntry;
import org.kuali.rice.krad.datadictionary.TransactionalDocumentEntry;
import org.kuali.rice.krad.document.TransactionalDocument;
import org.kuali.rice.krad.service.DataDictionaryService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.service.KualiModuleService;
import org.kuali.rice.krad.service.RiceApplicationConfigurationService;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.KRADUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiceApplicationConfigurationServiceImpl implements RiceApplicationConfigurationService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RiceApplicationConfigurationServiceImpl.class);
    
    protected List<Component> components = new ArrayList<Component>();
    private ConfigurationService kualiConfigurationService;
    private KualiModuleService kualiModuleService;
    private DataDictionaryService dataDictionaryService;

    /**
     * This method derived ParameterDetailedTypes from the DataDictionary for all BusinessObjects and Documents and from Spring for
     * all batch Steps.
     * 
     * @return List<ParameterDetailedType> containing the detailed types derived from the data dictionary and Spring
     */
    public List<Component> getNonDatabaseComponents() {
        if (components.isEmpty()) {
            Map<String, Component> uniqueParameterDetailTypeMap = new HashMap<String, Component>();
                        
            DataDictionaryService dataDictionaryService = KRADServiceLocatorWeb.getDataDictionaryService();
            
            //dataDictionaryService.getDataDictionary().forceCompleteDataDictionaryLoad();
            for (BusinessObjectEntry businessObjectEntry : dataDictionaryService.getDataDictionary().getBusinessObjectEntries().values()) {
                try {
                    Component parameterDetailType = getParameterDetailType((businessObjectEntry.getBaseBusinessObjectClass() != null) ? businessObjectEntry.getBaseBusinessObjectClass() : businessObjectEntry.getBusinessObjectClass());
                    uniqueParameterDetailTypeMap.put(parameterDetailType.getCode(), parameterDetailType);
                }
                catch (Exception e) {
                    LOG.error("The getDataDictionaryAndSpringComponents method of ParameterUtils encountered an exception while trying to create the detail type for business object class: " + businessObjectEntry.getBusinessObjectClass(), e);
                }
            }
            for (DocumentEntry documentEntry : dataDictionaryService.getDataDictionary().getDocumentEntries().values()) {
                if (documentEntry instanceof TransactionalDocumentEntry) {
                    try {
                        Component parameterDetailType = getParameterDetailType((documentEntry.getBaseDocumentClass() != null) ? documentEntry.getBaseDocumentClass() : documentEntry.getDocumentClass());
                        uniqueParameterDetailTypeMap.put(parameterDetailType.getCode(), parameterDetailType);
                    }
                    catch (Exception e) {
                        LOG.error("The getNonDatabaseDetailTypes encountered an exception while trying to create the detail type for transactional document class: " +
                        		((documentEntry.getBaseDocumentClass() != null) ? documentEntry.getBaseDocumentClass() : documentEntry.getDocumentClass()), e);
                    }
                }
            }
            components.addAll(uniqueParameterDetailTypeMap.values());
        }
        return Collections.unmodifiableList(components);
    }
    
    @SuppressWarnings("unchecked")
	protected Component getParameterDetailType(Class documentOrStepClass) {
        String detailTypeString = getKualiModuleService().getComponentCode(documentOrStepClass);

        String detailTypeName = getDetailTypeName(documentOrStepClass);

        String namespace = getKualiModuleService().getNamespaceCode(documentOrStepClass);
        String name = (detailTypeName == null) ? detailTypeString : detailTypeName;
        Component.Builder detailType = Component.Builder.create(namespace, detailTypeName, name);
        return detailType.build();
    }

    @SuppressWarnings("unchecked")
    /**
     * This method derived ParameterDetailedTypes from the DataDictionary for all BusinessObjects and Transactional Documents Entries and from Spring for
     * all batch Steps.
     * 
     * @return String containing the detailed type name derived from the data dictionary/Business Object
     */
	protected String getDetailTypeName(Class documentOrStepClass) {
        if (documentOrStepClass == null) {
            throw new IllegalArgumentException("The getDetailTypeName method of ParameterRepositoryServiceImpl requires non-null documentOrStepClass");
        }
        
        /* 
         * Some business objects have a Component annotation that sets the value
         * of the classes annotaion.  This if block will test to see if it is there, try to get the 
         * component value from the Data Dictionary if the BusinessObjectEntry exists, if it doesn't
         * exist, it will fall back to the annotation's value.
         */
        if (documentOrStepClass.isAnnotationPresent(COMPONENT.class)) {
            BusinessObjectEntry boe = getDataDictionaryService().getDataDictionary().getBusinessObjectEntry(documentOrStepClass.getName());
            if (boe != null) {
                return boe.getObjectLabel();
            }
            else {
                return ((COMPONENT) documentOrStepClass.getAnnotation(COMPONENT.class)).component();
            }
        }

        /*
         * If block that determines if the class is either a BusinessObject or a TransactionalDocument
         * return calls try to either get the BusinessObjectEntry's ObjectLable, or grabbing the 
         * data dictionary's BusinessTitleForClass if it is a BusinessObject, or the DocumentLabel if it is a
         * TransactionalDocument
         */
        if (TransactionalDocument.class.isAssignableFrom(documentOrStepClass)) {
            return getDataDictionaryService().getDocumentLabelByClass(documentOrStepClass);
        }
        else if (BusinessObject.class.isAssignableFrom(documentOrStepClass) ) {
            BusinessObjectEntry boe = getDataDictionaryService().getDataDictionary().getBusinessObjectEntry(documentOrStepClass.getName());
            if (boe != null) {
                return boe.getObjectLabel();
            }
            else {
                return KRADUtils.getBusinessTitleForClass(documentOrStepClass);
            }
        }
        throw new IllegalArgumentException("The getDetailTypeName method of ParameterRepositoryServiceImpl requires TransactionalDocument, BusinessObject, or Step class. Was: " + documentOrStepClass.getName() );
    }
    
    protected ConfigurationService getKualiConfigurationService() {
		if (kualiConfigurationService == null) {
			kualiConfigurationService = KRADServiceLocator.getKualiConfigurationService();
		}
		return kualiConfigurationService;
	}
    
    protected KualiModuleService getKualiModuleService() {
    	if (kualiModuleService == null) {
    		kualiModuleService = KRADServiceLocatorWeb.getKualiModuleService();
    	}
    	return kualiModuleService;
    }

    protected DataDictionaryService getDataDictionaryService() {
    	if (dataDictionaryService == null) {
    		dataDictionaryService = KRADServiceLocatorWeb.getDataDictionaryService();
    	}
    	return dataDictionaryService;
    }

}
