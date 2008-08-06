/*
 * Copyright 2006-2007 The Kuali Foundation.
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
package org.kuali.rice.kns.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.rice.KNSServiceLocator;
import org.kuali.rice.kns.KualiModule;
import org.kuali.rice.kns.bo.user.UniversalUser;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRule;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.kns.rule.event.ApproveDocumentEvent;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.RiceKeyConstants;
import org.kuali.rice.kns.web.format.PhoneNumberFormatter;

public class UniversalUserRule extends MaintenanceDocumentRuleBase {

    private UniversalUser oldUser;
    private UniversalUser newUser;
    
    private static final PhoneNumberFormatter phoneNumberFormatter = new PhoneNumberFormatter();

    private static String userEditWorkgroupName;
    private static List<KualiModule> installedModules;

    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean success = true;
        setupConvenienceObjects(document);
        // get the group name that we need here
        // only check if the user can modify the universal user attributes
        if ( GlobalVariables.getUserSession().getUniversalUser().isMember( userEditWorkgroupName ) ) {
            success &= checkGeneralRules(document);
        }
        MaintenanceDocumentRule rule = null;
        for ( KualiModule module : installedModules ) {
            rule = module.getModuleUserRule();
            if ( rule != null ) {
                success &= rule.processRouteDocument( document );
            }
        }        
        return success;
    }

    protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document) {
        boolean success = true;
        setupConvenienceObjects(document);
        // only check if the user can modify the universal user attributes
        if ( GlobalVariables.getUserSession().getUniversalUser().isMember( userEditWorkgroupName ) ) {
            success &= checkGeneralRules(document);
        }
        // save always succeeds even if there are rule violations
        MaintenanceDocumentRule rule = null;
        for ( KualiModule module : installedModules ) {
            rule = module.getModuleUserRule();
            if ( rule != null ) {
                success &= rule.processSaveDocument( document );
            }
        }
        return true;
    }

    /**
     * 
     * This method sets the convenience objects like newAccount and oldAccount, so you have short and easy handles to the new and
     * old objects contained in the maintenance document.
     * 
     * It also calls the BusinessObjectBase.refresh(), which will attempt to load all sub-objects from the DB by their primary keys,
     * if available.
     * 
     * @param document - the maintenanceDocument being evaluated
     * 
     */
    private void setupConvenienceObjects(MaintenanceDocument document) {

        // setup oldAccount convenience objects, make sure all possible sub-objects are populated
        oldUser = (UniversalUser) document.getOldMaintainableObject().getBusinessObject();
        //KFSMI-798 - refreshNonUpdatableReferences() used instead of refresh(), 
        //UniversalUser does not have any updatable references
        oldUser.refreshNonUpdateableReferences();

        // setup newAccount convenience objects, make sure all possible sub-objects are populated
        newUser = (UniversalUser) document.getNewMaintainableObject().getBusinessObject();
        //KFSMI-798 - refreshNonUpdatableReferences() used instead of refresh(), 
        //since UniversalUser does not have any updatable references
        newUser.refreshNonUpdateableReferences();

        if ( userEditWorkgroupName == null ) {
            userEditWorkgroupName = configService.getParameterValue(KNSConstants.KNS_NAMESPACE, KNSConstants.DetailTypes.UNIVERSAL_USER_DETAIL_TYPE, KNSConstants.CoreApcParms.UNIVERSAL_USER_EDIT_WORKGROUP);
            installedModules = KNSServiceLocator.getKualiModuleService().getInstalledModules();
        }
    }

    private boolean checkGeneralRules(MaintenanceDocument document)  {
        
        boolean success = true;
        
        
        //KULCOA-1164: Validation phone number
        
        String phoneNumber = newUser.getPersonLocalPhoneNumber();
        try {
            newUser.setPersonLocalPhoneNumber((String) phoneNumberFormatter.convertFromPresentationFormat(newUser.getPersonLocalPhoneNumber()));
        } catch (Exception e){
            putFieldError("personLocalPhoneNumber", RiceKeyConstants.ERROR_INVALID_FORMAT, new String[] {"Local Phone Number", phoneNumber});
            success = false;
        }
        
        if (oldUser==null) {
            oldUser=new UniversalUser();
        }
        
        // KULCOA-1164: Check whether User Id is unique or not
        
        String userId=newUser.getPersonUserIdentifier();
        if (userId != null && (!userId.equals(oldUser.getPersonUserIdentifier()) || "Copy".equals(document.getNewMaintainableObject().getMaintenanceAction()))) {
            if (userExists("personUserIdentifier", userId)) {
                putFieldError("personUserIdentifier", RiceKeyConstants.ERROR_DOCUMENT_MAINTENANCE_KEYS_ALREADY_EXIST_ON_CREATE_NEW, userId); 
                success = false;
            }
        }
        
        String emplId=newUser.getPersonPayrollIdentifier();
        // KULCOA-1164: Check whether Employee Id is unique or not
        if( emplId!= null && (!emplId.equals(oldUser.getPersonPayrollIdentifier()) || "Copy".equals(document.getNewMaintainableObject().getMaintenanceAction()))){
            if (userExists("personPayrollIdentifier", newUser.getPersonPayrollIdentifier())) {
                putFieldError("personPayrollIdentifier", RiceKeyConstants.ERROR_DOCUMENT_KUALIUSERMAINT_UNIQUE_EMPLID);    
                success = false;
            }
        }
        
  
        
        return success;
    }

    private boolean userExists(String field, String value) {

        Map searchMap = new HashMap();
        searchMap.put(field,value);
        
        return universalUserService.findUniversalUsers(searchMap).size() > 0;

        
    }
        
    @Override
    public boolean processApproveDocument(ApproveDocumentEvent approveEvent) {
	boolean success = super.processApproveDocument(approveEvent);

        // remove all items from the errorPath temporarily (because it may not
        // be what we expect, or what we need)
        clearErrorPath();
        
        // loop over all installed modules and run their user rules
        MaintenanceDocumentRule rule = null;
        for ( KualiModule module : installedModules ) {
            rule = module.getModuleUserRule();
            if ( rule != null ) {
                success &= rule.processApproveDocument( approveEvent );            
            }
        }

        // return the original set of items to the errorPath, to ensure no impact
        // on other upstream or downstream items that rely on the errorPath
        resumeErrorPath();

        return success;
    }

}
