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
package org.kuali.rice.kim.web.struts.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.bo.entity.dto.KimPrincipalInfo;
import org.kuali.rice.kim.bo.role.dto.KimRoleInfo;
import org.kuali.rice.kim.bo.types.impl.KimTypeImpl;
import org.kuali.rice.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.rice.kim.bo.ui.KimDocumentRoleQualifier;
import org.kuali.rice.kim.bo.ui.PersonDocumentRole;
import org.kuali.rice.kim.document.IdentityManagementPersonDocument;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.support.KimTypeService;
import org.kuali.rice.kim.util.KIMPropertyConstants;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kim.web.struts.form.IdentityManagementDocumentFormBase;
import org.kuali.rice.kim.web.struts.form.IdentityManagementPersonDocumentForm;
import org.kuali.rice.kns.datadictionary.AttributeDefinition;
import org.kuali.rice.kns.datadictionary.KimDataDictionaryAttributeDefinition;
import org.kuali.rice.kns.datadictionary.KimNonDataDictionaryAttributeDefinition;

/**
 * This is a description of what this class does - jonathan don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class IdentityManagementPersonInquiry extends IdentityManagementBaseInquiryAction {
		
	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kim.web.struts.action.IdentityManagementBaseInquiryAction#loadKimObject(javax.servlet.http.HttpServletRequest, org.kuali.rice.kim.web.struts.form.IdentityManagementDocumentFormBase)
	 */
	@Override
	protected void loadKimObject(HttpServletRequest request,
			IdentityManagementDocumentFormBase form) {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        String principalId = request.getParameter(KIMPropertyConstants.Person.PRINCIPAL_ID);
        String principalName = request.getParameter(KIMPropertyConstants.Person.PRINCIPAL_NAME);
        if ( StringUtils.isBlank(principalId) && StringUtils.isNotBlank(principalName) ) {
        	KimPrincipalInfo principal = KIMServiceLocator.getIdentityManagementService().getPrincipalByPrincipalName(principalName);
        	if ( principal != null ) {
        		principalId = principal.getPrincipalId();
        	}
        }
        if ( principalId != null ) {
        	personDocumentForm.setPrincipalId(principalId);
        	getUiDocumentService().loadEntityToPersonDoc(personDocumentForm.getPersonDocument(), personDocumentForm.getPrincipalId() );
        	populateRoleInformation( personDocumentForm.getPersonDocument() );
        }
	}

	protected void populateRoleInformation( IdentityManagementPersonDocument personDoc ) {
		for (PersonDocumentRole role : personDoc.getRoles()) {
	        KimTypeService kimTypeService = (KimTypeService)KIMServiceLocator.getService(getKimTypeServiceName(role.getKimRoleType()));
			role.setDefinitions(kimTypeService.getAttributeDefinitions(role.getKimTypeId()));
        	// when post again, it will need this during populate
            role.setNewRolePrncpl(new KimDocumentRoleMember());
            for (String key : role.getDefinitions().keySet()) {
            	KimDocumentRoleQualifier qualifier = new KimDocumentRoleQualifier();
            	//qualifier.setQualifierKey(key);
	        	setAttrDefnIdForQualifier(qualifier,role.getDefinitions().get(key));
            	role.getNewRolePrncpl().getQualifiers().add(qualifier);
            }
	        role.setAttributeEntry( getUiDocumentService().getAttributeEntries( role.getDefinitions() ) );
		}
	}
	
    private void setAttrDefnIdForQualifier(KimDocumentRoleQualifier qualifier,AttributeDefinition definition) {
    	if (definition instanceof KimDataDictionaryAttributeDefinition) {
    		qualifier.setKimAttrDefnId(((KimDataDictionaryAttributeDefinition)definition).getKimAttrDefnId());
    		qualifier.refreshReferenceObject("kimAttribute");
    	} else {
    		qualifier.setKimAttrDefnId(((KimNonDataDictionaryAttributeDefinition)definition).getKimAttrDefnId());
    		qualifier.refreshReferenceObject("kimAttribute");

    	}
    }
	private String getKimTypeServiceName (KimTypeImpl kimType) {
    	String serviceName = kimType.getKimTypeServiceName();
    	if (StringUtils.isBlank(serviceName)) {
    		serviceName = "kimTypeService";
    	}
    	return serviceName;

	}
	
}
