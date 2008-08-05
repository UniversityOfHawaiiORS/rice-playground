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
package org.kuali.rice.kew.actionrequests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.rice.kew.KEWServiceLocator;
import org.kuali.rice.kew.engine.RouteHelper;
import org.kuali.rice.kew.engine.node.RouteNodeInstance;
import org.kuali.rice.kew.engine.node.RouteNodeService;
import org.kuali.rice.kew.exception.WorkflowRuntimeException;

import edu.iu.uis.eden.util.PerformanceLogger;

/**
 * A service which effectively "refreshes" and requeus a document.  It first deletes any
 * pending action requests on the documents and then requeues the document for standard routing.
 * 
 * Intended to be called async and wired that way in server/client spring beans.
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class DocumentRequeuerImpl implements DocumentRequeuerService {
	
    private RouteHelper helper = new RouteHelper();
    
	public void requeueDocument(Long documentId) {
		PerformanceLogger performanceLogger = new PerformanceLogger();
        KEWServiceLocator.getRouteHeaderService().lockRouteHeader(documentId, true);
        Collection activeNodes = getRouteNodeService().getActiveNodeInstances(documentId);
        List<ActionRequestValue> requestsToDelete = new ArrayList<ActionRequestValue>();
        for (Iterator iterator = activeNodes.iterator(); iterator.hasNext();) {
            RouteNodeInstance nodeInstance = (RouteNodeInstance) iterator.next();
            // only "requeue" if we're dealing with a request activation node
            if (helper.isRequestActivationNode(nodeInstance.getRouteNode())) {
                requestsToDelete.addAll(getActionRequestService().findPendingRootRequestsByDocIdAtRouteNode(documentId, nodeInstance.getRouteNodeInstanceId()));
                // this will trigger a regeneration of requests
                nodeInstance.setInitial(true);
                getRouteNodeService().save(nodeInstance);
            }
        }
        for (Iterator iterator = requestsToDelete.iterator(); iterator.hasNext();) {
            ActionRequestValue actionRequestToDelete = (ActionRequestValue) iterator.next();
            // only delete the request if it was generated by a route module (or the rules system)
            if (actionRequestToDelete.isRouteModuleRequest()) {
                getActionRequestService().deleteActionRequestGraph(actionRequestToDelete);
            }
        }
        try {
        	KEWServiceLocator.getWorkflowEngine().process(documentId, null);
        } catch (Exception e) {
        	throw new WorkflowRuntimeException(e);
        }
        performanceLogger.log("Time to run DocumentRequeuer for document " + documentId);	
	}
    
    private ActionRequestService getActionRequestService() {
        return KEWServiceLocator.getActionRequestService();
    }
    
    private RouteNodeService getRouteNodeService() {
        return KEWServiceLocator.getRouteNodeService();
    }
}