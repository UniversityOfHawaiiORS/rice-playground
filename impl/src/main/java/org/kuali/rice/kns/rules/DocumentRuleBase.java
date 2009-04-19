/*
 * Copyright 2007 The Kuali Foundation.
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
package org.kuali.rice.kns.rules;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.IdentityManagementService;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.bo.AdHocRoutePerson;
import org.kuali.rice.kns.bo.AdHocRouteWorkgroup;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.TransactionalDocument;
import org.kuali.rice.kns.rule.AddAdHocRoutePersonRule;
import org.kuali.rice.kns.rule.AddAdHocRouteWorkgroupRule;
import org.kuali.rice.kns.rule.AddNoteRule;
import org.kuali.rice.kns.rule.ApproveDocumentRule;
import org.kuali.rice.kns.rule.RouteDocumentRule;
import org.kuali.rice.kns.rule.SaveDocumentRule;
import org.kuali.rice.kns.rule.SendAdHocRequestsRule;
import org.kuali.rice.kns.rule.event.ApproveDocumentEvent;
import org.kuali.rice.kns.service.DictionaryValidationService;
import org.kuali.rice.kns.service.DocumentHelperService;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.util.ErrorMap;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.KNSPropertyConstants;
import org.kuali.rice.kns.util.RiceKeyConstants;
import org.kuali.rice.kns.workflow.service.KualiWorkflowInfo;


/**
 * This class contains all of the business rules that are common to all documents.
 */
public abstract class DocumentRuleBase implements SaveDocumentRule, RouteDocumentRule, ApproveDocumentRule, AddNoteRule, AddAdHocRoutePersonRule, AddAdHocRouteWorkgroupRule, SendAdHocRequestsRule {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentRuleBase.class);

    private static PersonService personService;
    private static DictionaryValidationService dictionaryValidationService;
    private static KualiWorkflowInfo workflowInfoService;
    private static KualiConfigurationService kualiConfigurationService;
    private static DocumentHelperService documentHelperService;
    private static IdentityManagementService identityManagementService;

    /**
     * Just some arbitrarily high max depth that's unlikely to occur in real life to prevent recursion problems
     */
    private int maxDictionaryValidationDepth = 100;

    protected PersonService getPersonService() {
        if ( personService == null ) {
            personService = KIMServiceLocator.getPersonService();
        }
        return personService;
    }

    public static IdentityManagementService getIdentityManagementService() {
        if ( identityManagementService == null ) {
            identityManagementService = KIMServiceLocator.getIdentityManagementService();
        }
        return identityManagementService;
    }

    protected DocumentHelperService getDocumentHelperService() {
        if ( documentHelperService == null ) {
            documentHelperService = KNSServiceLocator.getDocumentHelperService();
        }
        return documentHelperService;
    }

    protected DictionaryValidationService getDictionaryValidationService() {
        if ( dictionaryValidationService == null ) {
            dictionaryValidationService = KNSServiceLocator.getDictionaryValidationService();
        }
        return dictionaryValidationService;
    }

    protected KualiWorkflowInfo getWorkflowInfoService() {
        if ( workflowInfoService == null ) {
            workflowInfoService = KNSServiceLocator.getWorkflowInfoService();
        }
        return workflowInfoService;
    }

    protected KualiConfigurationService getKualiConfigurationService() {
        if ( kualiConfigurationService == null ) {
            kualiConfigurationService = KNSServiceLocator.getKualiConfigurationService();
        }
        return kualiConfigurationService;
    }

    /**
     * Verifies that the document's overview fields are valid - it does required and format checks.
     * 
     * @param document
     * @return boolean True if the document description is valid, false otherwise.
     */
    public boolean isDocumentOverviewValid(Document document) {
        // add in the documentHeader path
        GlobalVariables.getErrorMap().addToErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);
        GlobalVariables.getErrorMap().addToErrorPath(KNSConstants.DOCUMENT_HEADER_PROPERTY_NAME);

        // check the document header for fields like the description
        getDictionaryValidationService().validateBusinessObject(document.getDocumentHeader());

        // drop the error path keys off now
        GlobalVariables.getErrorMap().removeFromErrorPath(KNSConstants.DOCUMENT_HEADER_PROPERTY_NAME);
        GlobalVariables.getErrorMap().removeFromErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);

        return GlobalVariables.getErrorMap().isEmpty();
    }

    /**
     * Validates the document attributes against the data dictionary.
     * 
     * @param document
     * @param validateRequired if true, then an error will be retruned if a DD required field is empty. if false, no required
     *        checking is done
     * @return True if the document attributes are valid, false otherwise.
     */
    public boolean isDocumentAttributesValid(Document document, boolean validateRequired) {
        // start updating the error path name
        GlobalVariables.getErrorMap().addToErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);

        // check the document for fields like explanation and org doc #
        getDictionaryValidationService().validateDocumentAndUpdatableReferencesRecursively(document, getMaxDictionaryValidationDepth(), validateRequired);

        // drop the error path keys off now
        GlobalVariables.getErrorMap().removeFromErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);

        return GlobalVariables.getErrorMap().isEmpty();
    }

    /**
     * Runs all business rules needed prior to saving. This includes both common rules for all documents, plus class-specific
     * business rules. This method will only return false if it fails the isValidForSave() test. Otherwise, it will always return
     * positive regardless of the outcome of the business rules. However, any error messages resulting from the business rules will
     * still be populated, for display to the consumer of this service.
     * 
     * @see org.kuali.rice.kns.rule.SaveDocumentRule#processSaveDocument(org.kuali.rice.kns.document.Document)
     */
    public boolean processSaveDocument(Document document) {
        boolean isValid = true;
        isValid = isDocumentOverviewValid(document);
        GlobalVariables.getErrorMap().addToErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);
        getDictionaryValidationService().validateDocumentAndUpdatableReferencesRecursively(document, getMaxDictionaryValidationDepth(), false);
        getDictionaryValidationService().validateDefaultExistenceChecksForTransDoc((TransactionalDocument) document);
        GlobalVariables.getErrorMap().removeFromErrorPath(KNSConstants.DOCUMENT_PROPERTY_NAME);
        isValid &= GlobalVariables.getErrorMap().isEmpty();
        isValid &= processCustomSaveDocumentBusinessRules(document);

        return isValid;
    }

    /**
     * This method should be overridden by children rule classes as a hook to implement document specific business rule checks for
     * the "save document" event.
     * 
     * @param document
     * @return boolean True if the rules checks passed, false otherwise.
     */
    protected boolean processCustomSaveDocumentBusinessRules(Document document) {
        return true;
    }

    /**
     * Runs all business rules needed prior to routing. This includes both common rules for all maintenance documents, plus
     * class-specific business rules. This method will return false if any business rule fails, or if the document is in an invalid
     * state, and not routable (see isDocumentValidForRouting()).
     * 
     * @see org.kuali.rice.kns.rule.RouteDocumentRule#processRouteDocument(org.kuali.rice.kns.document.Document)
     */
    public boolean processRouteDocument(Document document) {
        boolean isValid = true;

        isValid = isDocumentAttributesValid(document, true);

        // don't validate the document if the header is invalid
        if (isValid) {
            isValid &= processCustomRouteDocumentBusinessRules(document);
        }
        return isValid;
    }

    /**
     * This method should be overridden by children rule classes as a hook to implement document specific business rule checks for
     * the "route document" event.
     * 
     * @param document
     * @return boolean True if the rules checks passed, false otherwise.
     */
    protected boolean processCustomRouteDocumentBusinessRules(Document document) {
        return true;
    }

    /**
     * Runs all business rules needed prior to approving. This includes both common rules for all documents, plus class-specific
     * business rules. This method will return false if any business rule fails, or if the document is in an invalid state, and not
     * approveble.
     * 
     * @see org.kuali.rice.kns.rule.ApproveDocumentRule#processApproveDocument(org.kuali.rice.kns.rule.event.ApproveDocumentEvent)
     */
    public boolean processApproveDocument(ApproveDocumentEvent approveEvent) {
        boolean isValid = true;

        isValid = processCustomApproveDocumentBusinessRules(approveEvent);

        return isValid;
    }

    /**
     * This method should be overridden by children rule classes as a hook to implement document specific business rule checks for
     * the "approve document" event.
     * 
     * @param document
     * @return boolean True if the rules checks passed, false otherwise.
     */
    protected boolean processCustomApproveDocumentBusinessRules(ApproveDocumentEvent approveEvent) {
        return true;
    }

    /**
     * Runs all business rules needed prior to adding a document note. This method will return false if any business rule fails.
     * 
     * @see org.kuali.rice.kns.rule.AddDocumentNoteRule#processAddDocumentNote(org.kuali.rice.kns.document.Document,
     *      org.kuali.rice.kns.document.DocumentNote)
     */
    public boolean processAddNote(Document document, Note note) {
        boolean isValid = true;

        isValid &= isNoteValid(note);
        isValid &= processCustomAddNoteBusinessRules(document, note);

        return isValid;
    }

    /**
     * Verifies that the note's fields are valid - it does required and format checks.
     * 
     * @param note
     * @return boolean True if the document description is valid, false otherwise.
     */
    public boolean isNoteValid(Note note) {
        // add the error path keys on the stack
        GlobalVariables.getErrorMap().addToErrorPath(KNSConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME);

        // check the document header for fields like the description
        getDictionaryValidationService().validateBusinessObject(note);

        // drop the error path keys off now
        GlobalVariables.getErrorMap().removeFromErrorPath(KNSConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME);

        return GlobalVariables.getErrorMap().isEmpty();
    }

    /**
     * This method should be overridden by children rule classes as a hook to implement document specific business rule checks for
     * the "add document note" event.
     * 
     * @param document
     * @param note
     * @return boolean True if the rules checks passed, false otherwise.
     */
    protected boolean processCustomAddNoteBusinessRules(Document document, Note note) {
        return true;
    }

    /**
     * @see org.kuali.rice.kns.rule.AddAdHocRoutePersonRule#processAddAdHocRoutePerson(org.kuali.rice.kns.document.Document,
     *      org.kuali.rice.kns.bo.AdHocRoutePerson)
     */
    public boolean processAddAdHocRoutePerson(Document document, AdHocRoutePerson adHocRoutePerson) {
        boolean isValid = true;

        isValid &= isAddHocRoutePersonValid(document, adHocRoutePerson);

        isValid &= processCustomAddAdHocRoutePersonBusinessRules(document, adHocRoutePerson);
        return isValid;
    }

    
    /**
	 * @see org.kuali.rice.kns.rule.SendAdHocRequestsRule#processSendAdHocRequests(org.kuali.rice.kns.document.Document)
	 */
	public boolean processSendAdHocRequests(Document document) {
		// count on the generated events from 
		return processCustomSendAdHocRequests(document);
	}

	protected boolean processCustomSendAdHocRequests(Document document) {
		return true;
	}
	
	/**
     * Verifies that the adHocRoutePerson's fields are valid - it does required and format checks.
     * 
     * @param person
     * @return boolean True if valid, false otherwise.
     */
    public boolean isAddHocRoutePersonValid(Document document, AdHocRoutePerson person) {
        ErrorMap errorMap = GlobalVariables.getErrorMap();

        // new recipients are not embedded in the error path; existing lines should be
        if (errorMap.getErrorPath().size() == 0) {
            // add the error path keys on the stack
            errorMap.addToErrorPath(KNSConstants.NEW_AD_HOC_ROUTE_PERSON_PROPERTY_NAME);
        }
        
        if (StringUtils.isNotBlank(person.getId())) {
            Person user = getPersonService().getPersonByPrincipalName(person.getId());
            
            if (user == null) {
                GlobalVariables.getErrorMap().putError(KNSPropertyConstants.ID, RiceKeyConstants.ERROR_INVALID_ADHOC_PERSON_ID);
            }
            else if ( !getIdentityManagementService().hasPermission(user.getPrincipalId(), KimConstants.KIM_TYPE_DEFAULT_NAMESPACE, 
                    KimConstants.PermissionNames.LOG_IN, null) ) {
                GlobalVariables.getErrorMap().putError(KNSPropertyConstants.ID, RiceKeyConstants.ERROR_INACTIVE_ADHOC_PERSON_ID);
            }
            else {
                Class docOrBoClass = null;
                if (document instanceof MaintenanceDocument) {
                    docOrBoClass = ((MaintenanceDocument) document).getNewMaintainableObject().getBoClass();
                }
                else {
                    docOrBoClass = document.getClass();
                }
                if (!getDocumentHelperService().getDocumentAuthorizer(document).canReceiveAdHoc(document, user, person.getActionRequested())) {
                    GlobalVariables.getErrorMap().putError(KNSPropertyConstants.ID, RiceKeyConstants.ERROR_UNAUTHORIZED_ADHOC_PERSON_ID);
                }
            }
        }
        else {
            GlobalVariables.getErrorMap().putError(KNSPropertyConstants.ID, RiceKeyConstants.ERROR_MISSING_ADHOC_PERSON_ID);
        }

        // drop the error path keys off now
        errorMap.removeFromErrorPath(KNSConstants.NEW_AD_HOC_ROUTE_PERSON_PROPERTY_NAME);

        return GlobalVariables.getErrorMap().isEmpty();
    }

    /**
     * This method should be overridden by children rule classes as a hook to implement document specific business rule checks for
     * the "add ad hoc route person" event.
     * 
     * @param document
     * @param person
     * @return boolean True if the rules checks passed, false otherwise.
     */
    protected boolean processCustomAddAdHocRoutePersonBusinessRules(Document document, AdHocRoutePerson person) {
        return true;
    }

    /**
     * @see org.kuali.rice.kns.rule.AddAdHocRouteWorkgroupRule#processAddAdHocRouteWorkgroup(org.kuali.rice.kns.document.Document,
     *      org.kuali.rice.kns.bo.AdHocRouteWorkgroup)
     */
    public boolean processAddAdHocRouteWorkgroup(Document document, AdHocRouteWorkgroup adHocRouteWorkgroup) {
        boolean isValid = true;

        isValid &= isAddHocRouteWorkgroupValid(adHocRouteWorkgroup);

        isValid &= processCustomAddAdHocRouteWorkgroupBusinessRules(document, adHocRouteWorkgroup);
        return isValid;
    }

    /**
     * Verifies that the adHocRouteWorkgroup's fields are valid - it does required and format checks.
     * 
     * @param workgroup
     * @return boolean True if valid, false otherwise.
     */
    public boolean isAddHocRouteWorkgroupValid(AdHocRouteWorkgroup workgroup) {
        ErrorMap errorMap = GlobalVariables.getErrorMap();

        // new recipients are not embedded in the error path; existing lines should be
        if (errorMap.getErrorPath().size() == 0) {
            // add the error path keys on the stack
            GlobalVariables.getErrorMap().addToErrorPath(KNSConstants.NEW_AD_HOC_ROUTE_WORKGROUP_PROPERTY_NAME);
        }

        if (workgroup.getRecipientName() != null && workgroup.getRecipientNamespaceCode() != null) {
            // validate that they are a workgroup from the workgroup service by looking them up
            try {
                Group group = getIdentityManagementService().getGroupByName(workgroup.getRecipientNamespaceCode(), workgroup.getRecipientName());
                if (group == null || !group.isActive()) {
                    GlobalVariables.getErrorMap().putError(KNSPropertyConstants.ID, RiceKeyConstants.ERROR_INVALID_ADHOC_WORKGROUP_ID);
                }
            }
            catch (Exception e) {
                LOG.error("isAddHocRouteWorkgroupValid(AdHocRouteWorkgroup)", e);

                GlobalVariables.getErrorMap().putError(KNSPropertyConstants.ID, RiceKeyConstants.ERROR_INVALID_ADHOC_WORKGROUP_ID);
            }
        }
        else {
            GlobalVariables.getErrorMap().putError(KNSPropertyConstants.ID, RiceKeyConstants.ERROR_MISSING_ADHOC_WORKGROUP_ID);
        }

        // drop the error path keys off now
        GlobalVariables.getErrorMap().removeFromErrorPath(KNSConstants.NEW_AD_HOC_ROUTE_WORKGROUP_PROPERTY_NAME);

        return GlobalVariables.getErrorMap().isEmpty();
    }

    /**
     * This method should be overridden by children rule classes as a hook to implement document specific business rule checks for
     * the "add ad hoc route workgroup" event.
     * 
     * @param document
     * @param workgroup
     * @return boolean True if the rules checks passed, false otherwise.
     */
    protected boolean processCustomAddAdHocRouteWorkgroupBusinessRules(Document document, AdHocRouteWorkgroup workgroup) {
        return true;
    }

    /**
     * Gets the maximum number of levels the data-dictionary based validation will recurse for the document
     */
    public int getMaxDictionaryValidationDepth() {
        return this.maxDictionaryValidationDepth;
    }

    /**
     * Gets the maximum number of levels the data-dictionary based validation will recurse for the document
     */
    public void setMaxDictionaryValidationDepth(int maxDictionaryValidationDepth) {
        if (maxDictionaryValidationDepth < 0) {
            LOG.error("Dictionary validation depth should be greater than or equal to 0.  Value received was: " + maxDictionaryValidationDepth);
            throw new RuntimeException("Dictionary validation depth should be greater than or equal to 0.  Value received was: " + maxDictionaryValidationDepth);
        }
        this.maxDictionaryValidationDepth = maxDictionaryValidationDepth;
    }

}
