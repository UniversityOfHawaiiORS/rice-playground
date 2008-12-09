/*
 * Copyright 2007 The Kuali Foundation.
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
package org.kuali.rice.kns.document.authorization;

import java.util.Map;

import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.document.Document;

/**
 * The DocumentAuthorizer class associated with a given Document is used to dynamically determine what editing mode and what actions
 * are allowed for a given user on a given document instance.
 * 
 * 
 */
public interface DocumentAuthorizer {
    /**
     * @param document
     * @param user
     * @return Map with keys AuthorizationConstants.EditMode value (String) which indicates what operations the user is currently
     *         allowed to take on that document.
     */
    public Map getEditMode(Document document, Person user);

    /**
     * @param document - the document locks are to be established against or by
     * @param editMode - the editMode returned by the method {@link #getEditMode(Document, Person)}
     * @param user - the user locks are being established for
     * @return New map generated by locking logic combined with passed in parameter editMode.  Map contains keys 
     *         AuthorizationConstants.EditMode value (String) which indicates what operations the user is currently 
     *         allowed to take on that document.  This may be a modified list of 
     */
    public Map establishLocks(Document document, Map editMode, Person user);

    /**
     * @param document - the document to create the lock against and add the lock to
     */
    public void establishWorkflowPessimisticLocking(Document document);

    /**
     * @param document - document to release locks from
     */
    public void releaseWorkflowPessimisticLocking(Document document);
    
    /**
     * @param document
     * @param user
     * @return DocumentActionFlags instance indicating which actions are permitted the given user on the given document
     */
    public DocumentActionFlags getDocumentActionFlags(Document document, Person user);

    /**
     * @param documentTypeName
     * @param user
     * @return true if the given user is allowed to initiate documents of the given document type
     */
    public void canInitiate(String documentTypeName, Person user);
    
    /**
     * @param documentTypeName
     * @param user
     * @returns boolean indicating whether a user can copy a document
     */
    public boolean canCopy(String documentTypeName, Person user);

    /**
     * 
     * @param attachmentTypeName
     * @param document
     * @param user
     * @return
     */
    public boolean canViewAttachment(String attachmentTypeName, Document document, Person user);
    
    /**
     * Returns the appropriate map of qualification attributes for the current document.
     * 
     * The results of this method will be cached in a ThreadLocal for the duration of the request.
     * 
     * To populate this map, override the <b>populateRoleQualification</b> method defined in DocumentAuthorizerBase.
     */
    Map<String,String> getRoleQualification( Document document );
    
    /**
     * Returns the appropriate map of permission detail attributes for the current document.
     * 
     * The results of this method will be cached in a ThreadLocal for the duration of the request.
     * 
     * To populate this map, override the <b>populatePermissionDetails</b> method defined in DocumentAuthorizerBase.
     */
    Map<String,String> getPermissionDetailValues( Document document );
}

