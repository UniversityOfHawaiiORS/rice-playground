/*
 * Copyright 2005-2007 The Kuali Foundation
 * 
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
package org.kuali.rice.kew.actions.asyncservices;

import java.util.Set;

import org.kuali.rice.kew.actions.MoveDocumentAction;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.exception.WorkflowRuntimeException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kim.bo.entity.KimPrincipal;


/**
 * Service to do the async work of moving a document.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class MoveDocumentProcessor implements MoveDocumentService { 
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MoveDocumentProcessor.class);

	public void moveDocument(String principalId, DocumentRouteHeaderValue document, ActionTakenValue actionTaken, Set nodeNames) {
		KEWServiceLocator.getRouteHeaderService().lockRouteHeader(document.getRouteHeaderId(), true);
		KimPrincipal principal = KEWServiceLocator.getIdentityHelperService().getPrincipal(principalId);
		MoveDocumentAction moveAction = new MoveDocumentAction(document, principal, "", null);
        LOG.debug("Doing move document work " + document.getRouteHeaderId());
        try {
			moveAction.performDeferredMoveDocumentWork(actionTaken, nodeNames);
		} catch (Exception e) {
			throw new WorkflowRuntimeException(e);
		}
        LOG.debug("Work done and document requeued, document " + document.getRouteHeaderId());
	}
}
