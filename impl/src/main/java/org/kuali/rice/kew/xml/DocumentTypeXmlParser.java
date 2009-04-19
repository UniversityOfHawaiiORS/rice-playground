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
package org.kuali.rice.kew.xml;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kew.doctype.DocumentTypeAttribute;
import org.kuali.rice.kew.doctype.DocumentTypePolicy;
import org.kuali.rice.kew.doctype.DocumentTypePolicyEnum;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.document.DocumentTypeMaintainable;
import org.kuali.rice.kew.engine.node.ActivationTypeEnum;
import org.kuali.rice.kew.engine.node.BranchPrototype;
import org.kuali.rice.kew.engine.node.NodeType;
import org.kuali.rice.kew.engine.node.Process;
import org.kuali.rice.kew.engine.node.RoleNode;
import org.kuali.rice.kew.engine.node.RouteNode;
import org.kuali.rice.kew.engine.node.RouteNodeConfigParam;
import org.kuali.rice.kew.exception.InvalidParentDocTypeException;
import org.kuali.rice.kew.exception.InvalidXmlException;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.exception.WorkflowRuntimeException;
import org.kuali.rice.kew.export.ExportDataSet;
import org.kuali.rice.kew.role.RoleRouteModule;
import org.kuali.rice.kew.rule.FlexRM;
import org.kuali.rice.kew.rule.bo.RuleAttribute;
import org.kuali.rice.kew.rule.bo.RuleTemplate;
import org.kuali.rice.kew.rule.xmlrouting.XPathHelper;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.Utilities;
import org.kuali.rice.kew.util.XmlHelper;
import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.service.IdentityManagementService;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kns.exception.GroupNotFoundException;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.kns.util.MaintenanceUtils;
import org.kuali.rice.kns.util.ObjectUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * A parser for parsing an XML file into {@link DocumentType}s.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class DocumentTypeXmlParser implements XmlConstants {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentTypeXmlParser.class);

    /**
     * Default route node activation type to use if omitted
     */
    private static final String DEFAULT_ACTIVATION_TYPE = "S";

    public List docTypeRouteNodes;
    private Map nodesMap;
    private XPath xpath;
    private Group defaultExceptionWorkgroup;
    private static final String NEXT_NODE_EXP = "./@nextNode";
    private static final String PARENT_NEXT_NODE_EXP = "../@nextNode";
    
    protected XPath getXPath() {
        if (this.xpath == null) {
            this.xpath = XPathHelper.newXPath();
        }
        return xpath;
    }
    
    public List parseDocumentTypes(InputStream input) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, WorkflowException, TransformerException, GroupNotFoundException {
        Document routeDocument=XmlHelper.trimXml(input);
        Map documentTypesByName = new HashMap();
        for (Iterator iterator = parseAllDocumentTypes(routeDocument).iterator(); iterator.hasNext();) {
            DocumentType type = (DocumentType) iterator.next();
            documentTypesByName.put(type.getName(), type);
        }
        return new ArrayList(documentTypesByName.values());
    }

    /**
     * Parses all document types, both standard and routing.
     * 
     * @param routeDocument The DOM document to parse.
     * @return A list containing the desired document types.
     */
    private List<DocumentType> parseAllDocumentTypes(Document routeDocument) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, WorkflowException, TransformerException, GroupNotFoundException {
    	// A mapping from the names of uninitialized parent doc types to the child nodes that depend on the parent doc.
    	Map<String,List<DocTypeNode>> pendingChildDocs = new HashMap<String,List<DocTypeNode>>();
    	// A mapping from the names of uninitialized parent doc types to the names of the dependent children.
    	Map<String,List<String>> pendingChildNames = new HashMap<String,List<String>>();
    	// A stack containing Iterators over the various lists of unprocessed nodes; this allows for faster parent-child resolution
    	// without having to use recursion.
        List<Iterator<DocTypeNode>> docInitStack = new ArrayList<Iterator<DocTypeNode>>();
        // The first List of document types.
        List<DocTypeNode> initialList = new ArrayList<DocTypeNode>();
    	// The current size of the stack.
    	int stackLen = 0;
    	// The current Iterator instance.
    	Iterator<DocTypeNode> currentIter = null;
        // The current document type node.
        DocTypeNode currDocNode = null;

        List<DocumentType> docTypeBeans = new ArrayList<DocumentType>();
                
        // Acquire the "standard" and "routing" document types.
        NodeList initialNodes = null;
        xpath = XPathHelper.newXPath();
        initialNodes = (NodeList) xpath.evaluate("/" + DATA_ELEMENT + "/" + DOCUMENT_TYPES + "/" + DOCUMENT_TYPE, routeDocument, XPathConstants.NODESET);
        // Take each NodeList's nodes and insert them into a List implementation.
        for (int j = 0; j < initialNodes.getLength(); j++) {
            Node documentTypeNode = initialNodes.item(j);
            boolean docIsStandard = true;
            try {
                String xpathModeExpression = "./@" + DOCUMENT_TYPE_OVERWRITE_MODE;
                if (XmlHelper.pathExists(xpath, xpathModeExpression, documentTypeNode)) {
                    String overwriteMode = (String) xpath.evaluate(xpathModeExpression, documentTypeNode, XPathConstants.STRING);
                    docIsStandard = !StringUtils.equalsIgnoreCase("true", overwriteMode);
                }
            } catch (XPathExpressionException xpee) {
                LOG.error("Error trying to check for '" + DOCUMENT_TYPE_OVERWRITE_MODE + "' attribute on document type element", xpee);
                throw xpee;
            }
        	initialList.add(new DocTypeNode(documentTypeNode, docIsStandard));
        }

        // Setup the Iterator instance to start with.
        currentIter = initialList.iterator();
        
        // Keep looping until all Iterators are complete or an uncaught exception is thrown.
        while (stackLen >= 0) {
        	// Determine the action to take based on whether there are remaining nodes in the present iterator.
        	if (currentIter.hasNext()) {
        		// If the current iterator still has more nodes, process the next one.
        		String newParentName = null;
        		currDocNode = currentIter.next();
        		// Initialize the document, and catch any child initialization problems.
        		try {
        			// Take appropriate action based on whether the document is a standard one or a routing one.
        			DocumentType docType = parseDocumentTypes(!currDocNode.isStandard, routeDocument, currDocNode.docNode); 
        			// Insert into appropriate position in the final list, based on the doc type's location in the XML file's list.
      				docTypeBeans.add(docType);
            		// Store the document's name for reference.
            		newParentName = docType.getName();
        		}
        		catch (InvalidParentDocTypeException exc) {
        			// If the parent document has not been processed yet, then store the child document.
            		List<DocTypeNode> tempList = null;
            		List<String> tempStrList = null;
            		String parentName = exc.getParentName();
            		String childName = exc.getChildName();
            		if (parentName == null || childName == null) { // Make sure the parent & child documents' names are defined.
            			throw exc;
            		}
            		tempList = pendingChildDocs.get(parentName);
            		tempStrList = pendingChildNames.get(parentName);
            		if (tempList == null) { // Initialize a new child document list if necessary.
            			tempList = new ArrayList<DocTypeNode>();
            			tempStrList = new ArrayList<String>();
            			pendingChildDocs.put(parentName, tempList);
            			pendingChildNames.put(parentName, tempStrList);
            		}
        			tempList.add(currDocNode);
        			tempStrList.add(childName);
        		}
        		
            	// Check for any delayed child documents that are dependent on the current document.
        		List<DocTypeNode> childrenToProcess = pendingChildDocs.remove(newParentName);
        		pendingChildNames.remove(newParentName);
        		if (childrenToProcess != null) {
        			LOG.info("'" + newParentName + "' has children that were delayed; now processing them...");
        			// If there are any pending children, push the old Iterator onto the stack and process the new Iterator on the next
        			// iteration of the loop.
        			stackLen++;
        			docInitStack.add(currentIter);
        			currentIter = childrenToProcess.iterator();
        		}
        	}
        	else {
        		// If the current Iterator has reached its end, discard it and pop the next one (if any) from the stack.
        		stackLen--;
        		currentIter = ((stackLen >= 0) ? docInitStack.remove(stackLen) : null);
         	}
        }
        
        // Throw an error if there are still any uninitialized child documents.
        if (pendingChildDocs.size() > 0) {
        	StringBuilder errMsg = new StringBuilder("Invalid parent document types: ");
        	// Construct the error message.
        	for (Iterator<String> unknownParents = pendingChildNames.keySet().iterator(); unknownParents.hasNext();) {
        		String currParent = unknownParents.next();
        		errMsg.append("Invalid parent doc type '").append(currParent).append("' is needed by child doc types ");
        		for (Iterator<String> failedChildren = pendingChildNames.get(currParent).iterator(); failedChildren.hasNext();) {
        			String currChild = failedChildren.next();
        			errMsg.append('\'').append(currChild).append((failedChildren.hasNext()) ? "', " : "'; ");
        		}
        	}
        	// Throw the exception.
        	throw new InvalidParentDocTypeException(null, null, errMsg.toString());
        }
        
    	return docTypeBeans;
    }

    private DocumentType parseDocumentTypes(boolean isOverwrite, Document routeDocument, Node documentTypeNode) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, WorkflowException, TransformerException, GroupNotFoundException {
        DocumentType documentType = getFullDocumentType(isOverwrite, documentTypeNode);
        parseStructure(isOverwrite, documentTypeNode, routeDocument, documentType, new RoutePathContext());

        LOG.debug("Saving document type " + documentType.getName());
        routeDocumentType(documentType);
        return documentType;
    }

    private DocumentType getFullDocumentType(boolean isOverwrite, Node documentTypeNode) throws XPathExpressionException, GroupNotFoundException, InvalidXmlException, WorkflowException, SAXException, IOException, ParserConfigurationException {
        DocumentType documentType = getDocumentType(isOverwrite, documentTypeNode);
        /*
         * The following code does not need to apply the isOverwrite mode logic because it already checks to see if each node
         * is available on the ingested XML. If the node is ingested then it doesn't matter if we're in overwrite mode or not
         * the ingested code should save.
         */
        NodeList policiesList = (NodeList) xpath.evaluate("./" + POLICIES, documentTypeNode, XPathConstants.NODESET);
        if (policiesList.getLength() > 1) {
            // more than one <policies> tag is invalid
            throw new InvalidXmlException("More than one " + POLICIES + " node is present in a document type node");
        }
        else if (policiesList.getLength() > 0) {
            // if there is exactly one <policies> tag then parse it and use the values
            NodeList policyNodes = (NodeList) xpath.evaluate("./" + POLICY, policiesList.item(0), XPathConstants.NODESET);
            documentType.setPolicies(getDocumentTypePolicies(policyNodes, documentType));
        }

        NodeList attributeList = (NodeList) xpath.evaluate("./attributes", documentTypeNode, XPathConstants.NODESET);
        if (attributeList.getLength() > 1) {
            throw new InvalidXmlException("More than one attributes node is present in a document type node");
        }
        else if (attributeList.getLength() > 0) {
            NodeList attributeNodes = (NodeList) xpath.evaluate("./attribute", attributeList.item(0), XPathConstants.NODESET);
            documentType.setDocumentTypeAttributes(getDocumentTypeAttributes(attributeNodes, documentType));
        }

        NodeList securityList = (NodeList) xpath.evaluate("./" + SECURITY, documentTypeNode, XPathConstants.NODESET);
        if (securityList.getLength() > 1) {
            throw new InvalidXmlException("More than one " + SECURITY + " node is present in a document type node");
        }
        else if (securityList.getLength() > 0) {
           try {
             Node securityNode = securityList.item(0);
             String securityText = XmlHelper.writeNode(securityNode);
             documentType.setDocumentTypeSecurityXml(securityText);
           }
           catch (Exception e) {
             throw new InvalidXmlException(e);
           }
        }
        return documentType;
    }

    private void parseStructure(boolean isOverwrite, Node documentTypeNode, Document routeDocument, DocumentType documentType, RoutePathContext context) throws XPathExpressionException, InvalidXmlException, GroupNotFoundException, TransformerException {
        // TODO have a validation function that takes an xpath statement and blows chunks if that
        // statement returns false
        boolean hasRoutePathsElement = false;
        try {
            hasRoutePathsElement = XmlHelper.pathExists(xpath, "./" + ROUTE_PATHS, documentTypeNode);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type " + ROUTE_PATHS, xpee);
            throw xpee;
        }
        boolean hasRouteNodesElement = false;
        try {
            hasRouteNodesElement = XmlHelper.pathExists(xpath, "./" + ROUTE_NODES, documentTypeNode);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type " + ROUTE_NODES, xpee);
            throw xpee;
        }

        // check to see if we're in overwrite mode
        if (isOverwrite) {
            // since we're in overwrite mode, if we don't have a routeNodes element or a routePaths element we simply return
            if (!hasRouteNodesElement && !hasRoutePathsElement) {
                return;
            }
            // check to see if we have one but not the other element of routePaths and routeNodes
            else if (!hasRouteNodesElement || !hasRoutePathsElement) {
                // throw an exception since an ingestion can only have neither or both of the routePaths and routeNodes elements
                throw new InvalidXmlException("A overwriting document type ingestion can not have only one of the " + ROUTE_PATHS + " and " + ROUTE_NODES + " elements.  Either both or neither should be defined.");
            }
        }

        NodeList processNodes;

        try {
            if (XmlHelper.pathExists(xpath, "./" + ROUTE_PATHS + "/" + ROUTE_PATH, documentTypeNode)) {
                processNodes = (NodeList) xpath.evaluate("./" + ROUTE_PATHS + "/" + ROUTE_PATH, documentTypeNode, XPathConstants.NODESET);
            } else {
                return;
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type routePaths", xpee);
            throw xpee;
        }

        createProcesses(processNodes, documentType);

        NodeList nodeList = null;
        try {
            nodeList = (NodeList) xpath.evaluate("./" + ROUTE_PATHS + "/" + ROUTE_PATH + "/start", documentTypeNode, XPathConstants.NODESET);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type routePath start", xpee);
            throw xpee;
        }
        if (nodeList.getLength() > 1) {
            throw new InvalidXmlException("More than one start node is present in route path");
        } else if (nodeList.getLength() == 0) {
            throw new InvalidXmlException("No start node is present in route path");
        }
        try {
            nodeList = (NodeList) xpath.evaluate(".//" + ROUTE_NODES, documentTypeNode, XPathConstants.NODESET);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type routeNodes", xpee);
            throw xpee;
        }
        if (nodeList.getLength() > 1) {
            throw new InvalidXmlException("More than one routeNodes node is present in documentType node");
        } else if (nodeList.getLength() == 0) {
            throw new InvalidXmlException("No routeNodes node is present in documentType node");
        }
        Node routeNodesNode = nodeList.item(0);
        checkForOrphanedRouteNodes(documentTypeNode, routeNodesNode);

        // passed validation.
        nodesMap = new HashMap();
        for (int index = 0; index < processNodes.getLength(); index++) {
            Node processNode = processNodes.item(index);
            String startName;
            try {
                startName = (String) xpath.evaluate("./start/@name", processNode, XPathConstants.STRING);
            } catch (XPathExpressionException xpee) {
                LOG.error("Error obtaining routePath start name attribute", xpee);
                throw xpee;
            }
            String processName = KEWConstants.PRIMARY_PROCESS_NAME;
            if (Utilities.isEmpty(startName)) {
                try {
                    startName = (String) xpath.evaluate("./@" + INITIAL_NODE, processNode, XPathConstants.STRING);
                } catch (XPathExpressionException xpee) {
                    LOG.error("Error obtaining routePath initialNode attribute", xpee);
                    throw xpee;
                }
                try {
                    processName = (String) xpath.evaluate("./@" + PROCESS_NAME, processNode, XPathConstants.STRING);
                } catch (XPathExpressionException xpee) {
                    LOG.error("Error obtaining routePath processName attribute", xpee);
                    throw xpee;
                }
                if (Utilities.isEmpty(startName)) {
                    throw new InvalidXmlException("Invalid routePath: no initialNode attribute defined!");
                }
            }
            RouteNode routeNode = createRouteNode(null, startName, processNode, routeNodesNode, documentType, context);
            if (routeNode != null) {
                Process process = documentType.getNamedProcess(processName);
                process.setInitialRouteNode(routeNode);
            }
        }

    }

    private DocumentType getDocumentType(boolean isOverwrite, Node documentTypeNode) throws XPathExpressionException, GroupNotFoundException, InvalidXmlException, WorkflowException, SAXException, IOException, ParserConfigurationException {
        DocumentType documentType = null;
        String documentTypeName = getDocumentTypeNameFromNode(documentTypeNode);
        DocumentType previousDocumentType = KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
        if (isOverwrite) {
            // we don't need the isOverwrite value to be passed here because we're only temporarily creating this document type in memory
            documentType = generateNewDocumentTypeFromExisting(documentTypeName);
            // export the document type that exists in the database
        }
        // if we don't have a valid value for documentType create a brand new instance
        if (ObjectUtils.isNull(documentType)) {
            documentType = new DocumentType();
        }
        documentType.setName(documentTypeName);

        // set the description on the document type
        // the description is always taken from the previous document type unless specified in the ingested file
        String description = null;
        try {
            description = (String) xpath.evaluate("./" + DESCRIPTION, documentTypeNode, XPathConstants.STRING);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type description", xpee);
            throw xpee;
        }
        // if the ingestion has produced a valid value then set it on the document
        if (StringUtils.isNotBlank(description)) {
            documentType.setDescription(description);
        }
        // at this point we know the ingested value is blank
        else if (!isOverwrite) {
            // if this is not an overwrite we need to check the previous document type version for a value to pull forward
            if ( (ObjectUtils.isNotNull(previousDocumentType)) && (StringUtils.isNotBlank(previousDocumentType.getDescription())) ) {
                // keep the same value from the previous version of the document type from the database
                description = previousDocumentType.getDescription();
            }
            documentType.setDescription(description);
        }

        // set the label on the document type
        String label = null;
        try {
            label = (String) xpath.evaluate("./" + LABEL, documentTypeNode, XPathConstants.STRING);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type label", xpee);
            throw xpee;
        }
        // if the ingestion has produced a valid value then set it on the document
        if (StringUtils.isNotBlank(label)) {
            documentType.setLabel(label);
        }
        // at this point we know the ingested value is blank
        else if (!isOverwrite) {
            // if this is not an overwrite we need to check the previous document type version for a value to pull forward
            if (ObjectUtils.isNotNull(previousDocumentType) && StringUtils.isNotBlank(previousDocumentType.getLabel())) {
                // keep the same value from the previous version of the document type from the database
                label = previousDocumentType.getLabel();
            } else {
                // otherwise set it to undefined
                label = KEWConstants.DEFAULT_DOCUMENT_TYPE_LABEL;
            }
            documentType.setLabel(label);
        }

        // set the post processor class on the document type
        try {
            /*
             * - if the element tag is ingested... take whatever value is given, even if it's empty 
             * - we disregard the isOverwrite because if the element tag does not exist in the ingestion
             * then the documentType should already carry the value from the previous document type
             */
            if (XmlHelper.pathExists(xpath, "./" + POST_PROCESSOR_NAME, documentTypeNode)) {
                documentType.setPostProcessorName((String) xpath.evaluate("./" + POST_PROCESSOR_NAME, documentTypeNode, XPathConstants.STRING));
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type postProcessorName", xpee);
            throw xpee;
        }

        // set the document handler URL on the document type
        try {
            /*
             * - if the element tag is ingested... take whatever value is given, even if it's empty 
             * - we disregard the isOverwrite because if the element tag does not exist in the ingestion
             * then the documentType should already carry the value from the previous document type
             */
            if (XmlHelper.pathExists(xpath, "./" + DOC_HANDLER, documentTypeNode)) {
                documentType.setUnresolvedDocHandlerUrl((String) xpath.evaluate("./" + DOC_HANDLER, documentTypeNode, XPathConstants.STRING));
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type docHandler", xpee);
            throw xpee;
        }

        // set the help definition URL on the document type
        String helpDefUrl = null;
        try {
            helpDefUrl = (String) xpath.evaluate("./" + HELP_DEFINITION_URL, documentTypeNode, XPathConstants.STRING);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type help definition url", xpee);
            throw xpee;
        }
        // if the ingestion has produced a valid value then set it on the document
        if (StringUtils.isNotBlank(helpDefUrl)) {
            documentType.setUnresolvedHelpDefinitionUrl(helpDefUrl);
        }
        // at this point we know the ingested value is blank
        else if (!isOverwrite) {
            // if this is not an overwrite, we need to check the previous document type version for a value to pull forward
            if ( (ObjectUtils.isNotNull(previousDocumentType)) && (StringUtils.isNotBlank(previousDocumentType.getUnresolvedHelpDefinitionUrl())) ) {
                // keep the same value from the previous version of the document type from the database
                helpDefUrl = previousDocumentType.getUnresolvedHelpDefinitionUrl();
            }
            documentType.setUnresolvedHelpDefinitionUrl(helpDefUrl);
        }

        // set the service namespace on the document type
        try {
            /*
             * - if the element tag is ingested... take whatever value is given, even if it's empty 
             * - we disregard the isOverwrite because if the element tag does not exist in the ingestion
             * then the documentType should already carry the value from the previous document type
             */
            if (XmlHelper.pathExists(xpath, "./" + SERVICE_NAMESPACE, documentTypeNode)) {
                documentType.setActualServiceNamespace((String) xpath.evaluate("./" + SERVICE_NAMESPACE, documentTypeNode, XPathConstants.STRING));
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type ServiceNamespace", xpee);
            throw xpee;
        }

        // set the notification from address on the document type
        try {
            /*
             * - if the element tag is ingested... take whatever value is given, even if it's empty 
             * - we disregard the isOverwrite because if the element tag does not exist in the ingestion
             * then the documentType should already carry the value from the previous document type
             */
            if (XmlHelper.pathExists(xpath, "./" + NOTIFICATION_FROM_ADDRESS, documentTypeNode)) {
                documentType.setActualNotificationFromAddress((String) xpath.evaluate("./" + NOTIFICATION_FROM_ADDRESS, documentTypeNode, XPathConstants.STRING));
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type " + NOTIFICATION_FROM_ADDRESS, xpee);
            throw xpee;
        }

        try {
            /*
             * - if the element tag is ingested... take whatever value is given, even if it's empty 
             * - we disregard the isOverwrite because if the element tag does not exist in the ingestion
             * then the documentType should already carry the value from the previous document type
             */
            if (XmlHelper.pathExists(xpath, "./" + CUSTOM_EMAIL_STYLESHEET, documentTypeNode)) {
                documentType.setCustomEmailStylesheet((String) xpath.evaluate("./" + CUSTOM_EMAIL_STYLESHEET, documentTypeNode, XPathConstants.STRING));
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type " + CUSTOM_EMAIL_STYLESHEET, xpee);
            throw xpee;
        }

        // any ingested document type by default becomes the current document type
        documentType.setCurrentInd(Boolean.TRUE);

        // set up the default exception workgroup for the document type
        String exceptionWg;
        String exceptionWgName;
        String exceptionWgNamespace;
        try {
            exceptionWg = (String) xpath.evaluate("./" + DEFAULT_EXCEPTION_WORKGROUP_NAME, documentTypeNode, XPathConstants.STRING);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type defaultExceptionWorkgroupName", xpee);
            throw xpee;
        }
        // we don't need to take the isOverwrite into account here because this ingestion method is a shortcut to use the same workgroup in all route nodes
        if (StringUtils.isNotBlank(exceptionWg)) {
            // allow core config parameter replacement in documenttype workgroups
            exceptionWg = Utilities.substituteConfigParameters(exceptionWg);
            exceptionWgName = Utilities.parseGroupName(exceptionWg);
            exceptionWgNamespace = Utilities.parseGroupNamespaceCode(exceptionWg);
            Group exceptionGroup = getIdentityManagementService().getGroupByName(exceptionWgNamespace, exceptionWgName);
            if(exceptionGroup == null) {
                throw new WorkflowRuntimeException("Exception workgroup name " + exceptionWgName + " does not exist");
            }
            documentType.setDefaultExceptionWorkgroup(exceptionGroup);
            defaultExceptionWorkgroup = exceptionGroup;
        }

        // set up the active indicator on the document type
        try {
             // if the element tag is ingested... take whatever value is given
            if (XmlHelper.pathExists(xpath, "./" + ACTIVE, documentTypeNode)) {
                documentType.setActive(Boolean.valueOf((String) xpath.evaluate("./" + ACTIVE, documentTypeNode, XPathConstants.STRING)));
            } 
            // if isOverwrite is false set the default value
            else if (!isOverwrite) {
                documentType.setActive(Boolean.TRUE);
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type active flag", xpee);
            throw xpee;
        }

        // check for a parent document type for the ingested document type
        boolean parentElementExists = false;
        try {
            parentElementExists = XmlHelper.pathExists(xpath, "./" + PARENT, documentTypeNode);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type parent", xpee);
            throw xpee;
        }
        /*
         * - if the element tag is ingested... take whatever value is given 
         * - we disregard the isOverwrite because if the element tag does not exist in the ingestion
         * then the documentType should already carry the value from the previous document type
         */
        if (parentElementExists) {
            // the tag was ingested so we'll use whatever the user attempted to set
            String parentDocumentTypeName = null;
            try {
                parentDocumentTypeName = (String) xpath.evaluate("./" + PARENT, documentTypeNode, XPathConstants.STRING);
            } catch (XPathExpressionException xpee) {
                LOG.error("Error obtaining document type parent", xpee);
                throw xpee;
            }
            DocumentType parentDocumentType = KEWServiceLocator.getDocumentTypeService().findByName(parentDocumentTypeName);
            if (parentDocumentType == null) {
                //throw new InvalidXmlException("Invalid parent document type: '" + parentDocumentTypeName + "'");
                LOG.info("Parent document type '" + parentDocumentTypeName +
                        "' could not be found; attempting to delay processing of '" + documentTypeName + "'...");
                throw new InvalidParentDocTypeException(parentDocumentTypeName, documentTypeName,
                        "Invalid parent document type: '" + parentDocumentTypeName + "'");
            }
            documentType.setDocTypeParentId(parentDocumentType.getDocumentTypeId());
        }

        // set the super user workgroup name on the document type
        try {
            /*
             * - if the element tag is ingested... take whatever value is given
             * - we disregard the isOverwrite because if the element tag does not exist in the ingestion
             * then the documentType should already carry the value from the previous document type
             */
            if (XmlHelper.pathExists(xpath, "./" + SUPER_USER_WORKGROUP_NAME, documentTypeNode)) {
                documentType.setSuperUserWorkgroupNoInheritence(retrieveValidKimGroup("./" + SUPER_USER_WORKGROUP_NAME, documentTypeNode));
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type superUserWorkgroupName", xpee);
            throw xpee;
        }

        // set the blanket approve workgroup name on the document type
        String blanketWorkGroup = null;
        String blanketApprovePolicy = null;
        try {
            // check if the blanket approve workgroup name element tag was set on the ingested document type and get value if it was
            if (XmlHelper.pathExists(xpath, "./" + BLANKET_APPROVE_WORKGROUP_NAME, documentTypeNode)) {
                blanketWorkGroup =(String) xpath.evaluate("./" + BLANKET_APPROVE_WORKGROUP_NAME, documentTypeNode, XPathConstants.STRING);
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type " + BLANKET_APPROVE_WORKGROUP_NAME, xpee);
            throw xpee;
        }
        try {
            // check if the blanket approve policy element tag was set on the ingested document type and get value if it was
            if (XmlHelper.pathExists(xpath, "./" + BLANKET_APPROVE_POLICY, documentTypeNode)) {
                blanketApprovePolicy =(String) xpath.evaluate("./" + BLANKET_APPROVE_POLICY, documentTypeNode, XPathConstants.STRING);
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type " + BLANKET_APPROVE_POLICY, xpee);
            throw xpee;
        }
        // first check to see if the user ingested both a workgroup name and a policy
        if (StringUtils.isNotBlank(blanketWorkGroup) && StringUtils.isNotBlank(blanketApprovePolicy)) {
            throw new InvalidXmlException("Only one of the blanket approve xml tags can be set");
        }
        else if (StringUtils.isNotBlank(blanketWorkGroup)) {
            if (isOverwrite) {
                // if overwrite mode is on we need to make sure we clear out the blanket approve policy in case that was the previous document type's method
                documentType.setBlanketApprovePolicy(null);
            }
            documentType.setBlanketApproveWorkgroup(retrieveValidKimGroupUsingGroupName(blanketWorkGroup, documentTypeNode));
        }
        else if (StringUtils.isNotBlank(blanketApprovePolicy)) {
            if (isOverwrite) {
                // if overwrite mode is on we need to make sure we clear out the blanket approve workgroup in case that was the previous document type's method
                documentType.setBlanketApproveWorkgroup(null);
            }
            documentType.setBlanketApprovePolicy(blanketApprovePolicy);
        }

        // set the reporting workgroup name on the document type
        try {
            if (XmlHelper.pathExists(xpath, "./" + REPORTING_WORKGROUP_NAME, documentTypeNode)) {
                documentType.setReportingWorkgroup(retrieveValidKimGroup("./" + REPORTING_WORKGROUP_NAME, documentTypeNode));
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type " + REPORTING_WORKGROUP_NAME, xpee);
            throw xpee;
        }

        // set the routing version on the document type
        try {
            /*
             * - if the element tag is ingested... take whatever value is given 
             * - we disregard the isOverwrite because if the element tag does not exist in the ingestion
             * then the documentType should already carry the value from the previous document type
             */
            if (XmlHelper.pathExists(xpath, "./" + ROUTING_VERSION, documentTypeNode)) {
                String version;
                try {
                    version = (String) xpath.evaluate("./" + ROUTING_VERSION, documentTypeNode, XPathConstants.STRING);
                } catch (XPathExpressionException xpee) {
                    LOG.error("Error obtaining document type routingVersion", xpee);
                    throw xpee;
                }
                // verify that the routing version is one of the two valid values
                if (!(version.equals(KEWConstants.ROUTING_VERSION_ROUTE_LEVEL) || version.equals(KEWConstants.ROUTING_VERSION_NODAL))) {
                    throw new WorkflowRuntimeException("Invalid routing version on document type: " + version);
                }
                documentType.setRoutingVersion(version);
            }
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type routingVersion", xpee);
            throw xpee;
        }

        return documentType;
    }

    private Group retrieveValidKimGroup(String xpathExpression, Node documentTypeNode) throws XPathExpressionException, GroupNotFoundException {
        String unparsedGroupName;
        try {
            unparsedGroupName = (String) xpath.evaluate(xpathExpression, documentTypeNode, XPathConstants.STRING);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type workgroup using xpath expression: " + xpathExpression, xpee);
            throw xpee;
        }
        return retrieveValidKimGroupUsingGroupName(unparsedGroupName, documentTypeNode);
    }

    private Group retrieveValidKimGroupUsingGroupName(String unparsedGroupName, Node documentTypeNode) throws GroupNotFoundException {
        String groupNamespace;
        String groupName;
        // allow core config parameter replacement in documenttype workgroups
        unparsedGroupName = Utilities.substituteConfigParameters(unparsedGroupName);
        groupName = Utilities.parseGroupName(unparsedGroupName);
        groupNamespace = Utilities.parseGroupNamespaceCode(unparsedGroupName);
        Group workgroup = getIdentityManagementService().getGroupByName(groupNamespace, groupName);
        if (workgroup == null) {
            throw new GroupNotFoundException("Valid Workgroup could not be found... Namespace: " + groupNamespace + "  Name: " + groupName);
        }
        return workgroup;
    }

    public DocumentType generateNewDocumentTypeFromExisting(String documentTypeName) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, GroupNotFoundException, WorkflowException {
        // export the document type that exists in the database
        DocumentType docTypeFromDatabase = KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
        if (ObjectUtils.isNotNull(docTypeFromDatabase)) {
            ExportDataSet exportDataSet = new ExportDataSet();
            exportDataSet.getDocumentTypes().add(docTypeFromDatabase);
            byte[] xmlBytes = KEWServiceLocator.getXmlExporterService().export(exportDataSet);
            // use the exported document type from the database to generate the new document type
            Document tempDocument = XmlHelper.trimXml(new BufferedInputStream(new ByteArrayInputStream(xmlBytes)));
            Node documentTypeNode = (Node) getXPath().evaluate("/" + DATA_ELEMENT + "/" + DOCUMENT_TYPES + "/" + DOCUMENT_TYPE, tempDocument, XPathConstants.NODE);
            return getFullDocumentType(false, documentTypeNode);
        }
        return null;
    }

    private void routeDocumentType(DocumentType documentType) {
        DocumentType docType = KEWServiceLocator.getDocumentTypeService().findByName(documentType.getName());
        // if the docType exists then check locking
        if (ObjectUtils.isNotNull(docType)) {
            Maintainable docTypeMaintainable = new DocumentTypeMaintainable();
            docTypeMaintainable.setBusinessObject(docType);
            docTypeMaintainable.setBoClass(docType.getClass());
            // below will throw a ValidationException if a valid locking document exists
            MaintenanceUtils.checkForLockingDocument(docTypeMaintainable);
        }
        KEWServiceLocator.getDocumentTypeService().versionAndSave(documentType);
    }

    private String getDocumentTypeNameFromNode(Node documentTypeNode) throws XPathExpressionException {
        try {
            return (String) xpath.evaluate("./name", documentTypeNode, XPathConstants.STRING);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining document type name", xpee);
            throw xpee;
        }
    }

    /**
     * Checks for route nodes that are declared but never used and throws an InvalidXmlException if one is discovered.
     */
    private void checkForOrphanedRouteNodes(Node documentTypeNode, Node routeNodesNode) throws XPathExpressionException, InvalidXmlException {
        NodeList nodesInPath = (NodeList) xpath.evaluate("./routePaths/routePath//*/@name", documentTypeNode, XPathConstants.NODESET);
        List<String> nodeNamesInPath = new ArrayList<String>(nodesInPath.getLength());
        for (int index = 0; index < nodesInPath.getLength(); index++) {
            Node nameNode = nodesInPath.item(index);
            nodeNamesInPath.add(nameNode.getNodeValue());
        }

        NodeList declaredNodes = (NodeList) xpath.evaluate("./*/@name", routeNodesNode, XPathConstants.NODESET);
        List<String> declaredNodeNames = new ArrayList<String>(declaredNodes.getLength());
        for (int index = 0; index < declaredNodes.getLength(); index++) {
            Node nameNode = declaredNodes.item(index);
            declaredNodeNames.add(nameNode.getNodeValue());
        }

        // now compare the declared nodes to the ones actually used
        List<String> orphanedNodes = new ArrayList<String>();
        for (String declaredNode : declaredNodeNames) {
            boolean foundNode = false;
            for (String nodeInPath : nodeNamesInPath) {
                if (nodeInPath.equals(declaredNode)) {
                    foundNode = true;
                    break;
                }
            }
            if (!foundNode) {
                orphanedNodes.add(declaredNode);
            }
        }
        if (!orphanedNodes.isEmpty()) {
            String message = "The following nodes were declared but never used: ";
            for (Iterator iterator = orphanedNodes.iterator(); iterator.hasNext();) {
                String orphanedNode = (String) iterator.next();
                message += orphanedNode + (iterator.hasNext() ? ", " : "");
            }
            throw new InvalidXmlException(message);
        }
    }

    private void createProcesses(NodeList processNodes, DocumentType documentType) {
        for (int index = 0; index < processNodes.getLength(); index++) {
            Node processNode = processNodes.item(index);
            NamedNodeMap attributes = processNode.getAttributes();
            Node processNameNode = attributes.getNamedItem(PROCESS_NAME);
            String processName = (processNameNode == null ? null : processNameNode.getNodeValue());
            Process process = new Process();
            if (Utilities.isEmpty(processName)) {
                process.setInitial(true);
                process.setName(KEWConstants.PRIMARY_PROCESS_NAME);
            } else {
                process.setInitial(false);
                process.setName(processName);
            }
            process.setDocumentType(documentType);
            documentType.addProcess(process);
        }
    }

    private RouteNode createRouteNode(RouteNode previousRouteNode, String nodeName, Node routePathNode, Node routeNodesNode, DocumentType documentType, RoutePathContext context) throws XPathExpressionException, InvalidXmlException, GroupNotFoundException, TransformerException {
        if (nodeName == null) return null;

        Node currentNode;
        try {
            currentNode = (Node) xpath.evaluate(".//*[@name = '" + nodeName + "']", routePathNode, XPathConstants.NODE);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining routePath for routeNode", xpee);
            throw xpee;
        }
        if (currentNode == null) {
            String message = "Next node '" + nodeName + "' for node '" + previousRouteNode.getRouteNodeName() + "' not found!";
            LOG.error(message);
            throw new InvalidXmlException(message);
        }
        boolean nodeIsABranch;
        try {
            nodeIsABranch = ((Boolean) xpath.evaluate("self::node()[local-name() = 'branch']", currentNode, XPathConstants.BOOLEAN)).booleanValue();
        } catch (XPathExpressionException xpee) {
            LOG.error("Error testing whether node is a branch", xpee);
            throw xpee;
        }
        if (nodeIsABranch) {
            throw new InvalidXmlException("Next node cannot be a branch node");
        }

        String localName;
        try {
            localName = (String) xpath.evaluate("local-name(.)", currentNode, XPathConstants.STRING);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining node local-name", xpee);
            throw xpee;
        }
        RouteNode currentRouteNode = null;
        if (nodesMap.containsKey(nodeName)) {
            currentRouteNode = (RouteNode) nodesMap.get(nodeName);
        } else {
            String nodeExpression = ".//*[@name='" + nodeName + "']";
            currentRouteNode = makeRouteNodePrototype(localName, nodeName, nodeExpression, routeNodesNode, documentType, context);
        }

        if ("split".equalsIgnoreCase(localName)) {
            getSplitNextNodes(currentNode, routePathNode, currentRouteNode, routeNodesNode, documentType, context);
        }

        if (previousRouteNode != null) {
            previousRouteNode.getNextNodes().add(currentRouteNode);
            nodesMap.put(previousRouteNode.getRouteNodeName(), previousRouteNode);
            currentRouteNode.getPreviousNodes().add(previousRouteNode);
        }

        String nextNodeName = null;
        boolean hasNextNodeAttrib;
        try {
            hasNextNodeAttrib = ((Boolean) xpath.evaluate(NEXT_NODE_EXP, currentNode, XPathConstants.BOOLEAN)).booleanValue();
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining node nextNode attrib", xpee);
            throw xpee;
        }
        if (hasNextNodeAttrib) {
            // if the node has a nextNode but is not a split node, the nextNode is used for its node
            if (!"split".equalsIgnoreCase(localName)) {
                try {
                    nextNodeName = (String) xpath.evaluate(NEXT_NODE_EXP, currentNode, XPathConstants.STRING);
                } catch (XPathExpressionException xpee) {
                    LOG.error("Error obtaining node nextNode attrib", xpee);
                    throw xpee;
                }
                createRouteNode(currentRouteNode, nextNodeName, routePathNode, routeNodesNode, documentType, context);
            } else {
                // if the node has a nextNode but is a split node, the nextNode must be used for that split node's join node
                nodesMap.put(currentRouteNode.getRouteNodeName(), currentRouteNode);
            }
        } else {
            // if the node has no nextNode of its own and is not a join which gets its nextNode from its parent split node
            if (!"join".equalsIgnoreCase(localName)) {
                nodesMap.put(currentRouteNode.getRouteNodeName(), currentRouteNode);
                // if join has a parent nextNode (on its split node) and join has not already walked this path
            } else {
                boolean parentHasNextNodeAttrib;
                try {
                    parentHasNextNodeAttrib = ((Boolean) xpath.evaluate(PARENT_NEXT_NODE_EXP, currentNode, XPathConstants.BOOLEAN)).booleanValue();
                } catch (XPathExpressionException xpee) {
                    LOG.error("Error obtaining parent node nextNode attrib", xpee);
                    throw xpee;
                }
                if (parentHasNextNodeAttrib && !nodesMap.containsKey(nodeName)) {
                    try {
                        nextNodeName = (String) xpath.evaluate(PARENT_NEXT_NODE_EXP, currentNode, XPathConstants.STRING);
                    } catch (XPathExpressionException xpee) {
                        LOG.error("Error obtaining parent node nextNode attrib", xpee);
                        throw xpee;
                    }
                    createRouteNode(currentRouteNode, nextNodeName, routePathNode, routeNodesNode, documentType, context);
                } else {
                    // if join's parent split node does not have a nextNode
                    nodesMap.put(currentRouteNode.getRouteNodeName(), currentRouteNode);
                }
            }
        }
        return currentRouteNode;
    }

    private void getSplitNextNodes(Node splitNode, Node routePathNode, RouteNode splitRouteNode, Node routeNodesNode, DocumentType documentType, RoutePathContext context) throws XPathExpressionException, InvalidXmlException, GroupNotFoundException, TransformerException {
        NodeList splitBranchNodes;
        try {
            splitBranchNodes = (NodeList) xpath.evaluate("./branch", splitNode, XPathConstants.NODESET);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error obtaining split node branch", xpee);
            throw xpee;
        }
        for (int i = 0; i < splitBranchNodes.getLength(); i++) {
            String branchName;
            try {
                branchName = (String) xpath.evaluate("./@name", splitBranchNodes.item(i), XPathConstants.STRING);
            } catch (XPathExpressionException xpee) {
                LOG.error("Error obtaining branch name attribute", xpee);
                throw xpee;
            }
            String name;
            try {
                name = (String) xpath.evaluate("./*[1]/@name", splitBranchNodes.item(i), XPathConstants.STRING);
            } catch (XPathExpressionException xpee) {
                LOG.error("Error obtaining first split branch node name", xpee);
                throw xpee;
            }
            context.branch = new BranchPrototype();
            context.branch.setName(branchName);

            createRouteNode(splitRouteNode, name, routePathNode, routeNodesNode, documentType, context);
        }
    }

    private RouteNode makeRouteNodePrototype(String nodeTypeName, String nodeName, String nodeExpression, Node routeNodesNode, DocumentType documentType, RoutePathContext context) throws XPathExpressionException, GroupNotFoundException, InvalidXmlException, TransformerException {
        NodeList nodeList;
        try {
            nodeList = (NodeList) xpath.evaluate(nodeExpression, routeNodesNode, XPathConstants.NODESET);
        } catch (XPathExpressionException xpee) {
            LOG.error("Error evaluating node expression: '" + nodeExpression + "'");
            throw xpee;
        }
        if (nodeList.getLength() > 1) {
            throw new InvalidXmlException("More than one node under routeNodes has the same name of '" + nodeName + "'");
        }
        if (nodeList.getLength() == 0) {
            throw new InvalidXmlException("No node definition was found with the name '" + nodeName + "'");
        }
        Node node = nodeList.item(0);

        RouteNode routeNode = new RouteNode();
        // set fields that all route nodes of all types should have defined
        routeNode.setDocumentType(documentType);
        routeNode.setRouteNodeName((String) xpath.evaluate("./@name", node, XPathConstants.STRING));
        routeNode.setContentFragment(XmlHelper.writeNode(node));

        if (XmlHelper.pathExists(xpath, "./activationType", node)) {
            routeNode.setActivationType(ActivationTypeEnum.parse((String) xpath.evaluate("./activationType", node, XPathConstants.STRING)).getCode());
        } else {
            routeNode.setActivationType(DEFAULT_ACTIVATION_TYPE);
        }

        Group exceptionWorkgroup = defaultExceptionWorkgroup;

        String exceptionWg = (String) xpath.evaluate("./exceptionWorkgroupName", node, XPathConstants.STRING);
        String exceptionWorkgroupName = Utilities.parseGroupName(exceptionWg);
        String exceptionWorkgroupNamespace = Utilities.parseGroupNamespaceCode(exceptionWg);

        if (Utilities.isEmpty(exceptionWorkgroupName)) {
            // for backward compatibility we also need to be able to support exceptionWorkgroup
            exceptionWg = (String) xpath.evaluate("./exceptionWorkgroup", node, XPathConstants.STRING);
            exceptionWorkgroupName = Utilities.parseGroupName(exceptionWg);
            exceptionWorkgroupNamespace = Utilities.parseGroupNamespaceCode(exceptionWg);
        }
        if (Utilities.isEmpty(exceptionWorkgroupName)) {
            if (routeNode.getDocumentType().getDefaultExceptionWorkgroup() != null) {
                exceptionWorkgroupName = routeNode.getDocumentType().getDefaultExceptionWorkgroup().getGroupName();
                exceptionWorkgroupNamespace = routeNode.getDocumentType().getDefaultExceptionWorkgroup().getNamespaceCode();
            }
        }
        if (!Utilities.isEmpty(exceptionWorkgroupName)) {
            exceptionWorkgroup = getIdentityManagementService().getGroupByName(exceptionWorkgroupNamespace, exceptionWorkgroupName);
            if (exceptionWorkgroup == null) {
                throw new GroupNotFoundException("Could not locate exception workgroup with namespace '" + exceptionWorkgroupNamespace + "' and name '" + exceptionWorkgroupName + "'");
            }
        }
        if (exceptionWorkgroup != null) {
            routeNode.setExceptionWorkgroupName(exceptionWorkgroup.getGroupName());
            routeNode.setExceptionWorkgroupId(exceptionWorkgroup.getGroupId());
        }

        if (((Boolean) xpath.evaluate("./mandatoryRoute", node, XPathConstants.BOOLEAN)).booleanValue()) {
            routeNode.setMandatoryRouteInd(Boolean.valueOf((String)xpath.evaluate("./mandatoryRoute", node, XPathConstants.STRING)));
        } else {
            routeNode.setMandatoryRouteInd(Boolean.FALSE);
        }
        if (((Boolean) xpath.evaluate("./finalApproval", node, XPathConstants.BOOLEAN)).booleanValue()) {
            routeNode.setFinalApprovalInd(Boolean.valueOf((String)xpath.evaluate("./finalApproval", node, XPathConstants.STRING)));
        } else {
            routeNode.setFinalApprovalInd(Boolean.FALSE);
        }

        // for every simple child element of the node, store a config parameter of the element name and text content
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element) {
                Element e = (Element) n;
                String name = e.getNodeName();
                String content = XmlHelper.getTextContent(e);
                routeNode.getConfigParams().add(new RouteNodeConfigParam(routeNode, name, content));
            }
        }

        // make sure a default rule selector is set
        Map<String, String> cfgMap = Utilities.getKeyValueCollectionAsMap(routeNode.getConfigParams());
        if (!cfgMap.containsKey(RouteNode.RULE_SELECTOR_CFG_KEY)) {
            routeNode.getConfigParams().add(new RouteNodeConfigParam(routeNode, RouteNode.RULE_SELECTOR_CFG_KEY, FlexRM.DEFAULT_RULE_SELECTOR));
        }

        if (((Boolean) xpath.evaluate("./ruleTemplate", node, XPathConstants.BOOLEAN)).booleanValue()) {
            String ruleTemplateName = (String) xpath.evaluate("./ruleTemplate", node, XPathConstants.STRING);
            RuleTemplate ruleTemplate = KEWServiceLocator.getRuleTemplateService().findByRuleTemplateName(ruleTemplateName);
            if (ruleTemplate == null) {
                throw new InvalidXmlException("Rule template for node '" + routeNode.getRouteNodeName() + "' not found: " + ruleTemplateName);
            }
            routeNode.setRouteMethodName(ruleTemplateName);
            routeNode.setRouteMethodCode(KEWConstants.ROUTE_LEVEL_FLEX_RM);
        } else if (((Boolean) xpath.evaluate("./routeModule", node, XPathConstants.BOOLEAN)).booleanValue()) {
            routeNode.setRouteMethodName((String) xpath.evaluate("./routeModule", node, XPathConstants.STRING));
            routeNode.setRouteMethodCode(KEWConstants.ROUTE_LEVEL_ROUTE_MODULE);
        }

        String nodeType = null;
        if (((Boolean) xpath.evaluate("./type", node, XPathConstants.BOOLEAN)).booleanValue()) {
            nodeType = (String) xpath.evaluate("./type", node, XPathConstants.STRING);
        } else {
            String localName = (String) xpath.evaluate("local-name(.)", node, XPathConstants.STRING);
            if ("start".equalsIgnoreCase(localName)) {
                nodeType = "org.kuali.rice.kew.engine.node.InitialNode";
            } else if ("split".equalsIgnoreCase(localName)) {
                nodeType = "org.kuali.rice.kew.engine.node.SimpleSplitNode";
            } else if ("join".equalsIgnoreCase(localName)) {
                nodeType = "org.kuali.rice.kew.engine.node.SimpleJoinNode";
            } else if ("requests".equalsIgnoreCase(localName)) {
                nodeType = "org.kuali.rice.kew.engine.node.RequestsNode";
            } else if ("process".equalsIgnoreCase(localName)) {
                nodeType = "org.kuali.rice.kew.engine.node.SimpleSubProcessNode";
            } else if (NodeType.ROLE.getName().equalsIgnoreCase(localName)) {
                nodeType = RoleNode.class.getName();
            }
        }
        if (Utilities.isEmpty(nodeType)) {
            throw new InvalidXmlException("Could not determine node type for the node named '" + routeNode.getRouteNodeName() + "'");
        }
        routeNode.setNodeType(nodeType);

        String localName = (String) xpath.evaluate("local-name(.)", node, XPathConstants.STRING);
        if ("split".equalsIgnoreCase(localName)) {
            context.splitNodeStack.addFirst(routeNode);
        } else if ("join".equalsIgnoreCase(localName) && context.splitNodeStack.size() != 0) {
            // join node should have same branch prototype as split node
            RouteNode splitNode = (RouteNode)context.splitNodeStack.removeFirst();
            context.branch = splitNode.getBranch();
        } else if (NodeType.ROLE.getName().equalsIgnoreCase(localName)) {
            routeNode.setRouteMethodName(RoleRouteModule.class.getName());
            routeNode.setRouteMethodCode(KEWConstants.ROUTE_LEVEL_ROUTE_MODULE);
        }
        routeNode.setBranch(context.branch);

        return routeNode;
    }

    private List getDocumentTypePolicies(NodeList documentTypePolicies, DocumentType documentType) throws XPathExpressionException, InvalidXmlException {
        List policies = new ArrayList();
        Set policyNames = new HashSet();

        for (int i = 0; i < documentTypePolicies.getLength(); i++) {
            DocumentTypePolicy policy = new DocumentTypePolicy();
            policy.setDocumentTypeId(documentType.getDocumentTypeId());
            try {
                String policyName = (String) xpath.evaluate("./name", documentTypePolicies.item(i), XPathConstants.STRING);
                policy.setPolicyName(DocumentTypePolicyEnum.lookup(policyName).getName());
            } catch (XPathExpressionException xpee) {
                LOG.error("Error obtaining document type policy name", xpee);
                throw xpee;
            }
            try {
                if (((Boolean) xpath.evaluate("./value", documentTypePolicies.item(i), XPathConstants.BOOLEAN)).booleanValue()) {
                    policy.setPolicyValue(Boolean.valueOf((String) xpath.evaluate("./value", documentTypePolicies.item(i), XPathConstants.STRING)));
                } else {
                    policy.setPolicyValue(Boolean.FALSE);
                }
            } catch (XPathExpressionException xpee) {
                LOG.error("Error obtaining document type policy value", xpee);
                throw xpee;
            }
            if (!policyNames.add(policy.getPolicyName())) {
                throw new InvalidXmlException("Policy '" + policy.getPolicyName() + "' has already been defined on this document");
            } else {
                policies.add(policy);
            }
        }

        return policies;
    }

    private List getDocumentTypeAttributes(NodeList documentTypeAttributes, DocumentType documentType) throws XPathExpressionException, WorkflowException {
        List attributes = new ArrayList();

        for (int i = 0; i < documentTypeAttributes.getLength(); i++) {
            DocumentTypeAttribute attribute = new DocumentTypeAttribute();
            attribute.setDocumentType(documentType);
            String ruleAttributeName;
            try {
                ruleAttributeName = (String) xpath.evaluate("./name", documentTypeAttributes.item(i), XPathConstants.STRING);
            } catch (XPathExpressionException xpee) {
                LOG.error("Error obtaining rule attribute name", xpee);
                throw xpee;
            }
            RuleAttribute ruleAttribute = KEWServiceLocator.getRuleAttributeService().findByName(ruleAttributeName);
            if (ruleAttribute == null) {
                throw new WorkflowException("Could not find rule attribute: " + ruleAttributeName);
            }
            attribute.setDocumentType(documentType);
            attribute.setRuleAttribute(ruleAttribute);
            attribute.setOrderIndex(i+1);
            attributes.add(attribute);
        }
        return attributes;
    }

    private class RoutePathContext {
        public BranchPrototype branch;
        public LinkedList splitNodeStack = new LinkedList();
    }

    protected IdentityManagementService getIdentityManagementService() {
        return KIMServiceLocator.getIdentityManagementService();
    }

    /**
     * This is a helper class for indicating if an unprocessed document type node is "standard" or "routing."
     * 
     * @author Kuali Rice Team (kuali-rice@googlegroups.com)
     */
    private class DocTypeNode {
        /** The Node that needs to be converted into a doc type. */
        public final Node docNode;
        /** A flag for indicating the document's type; true indicates "standard," false indicates "routing." */
        public final boolean isStandard;
        
        /**
         * Constructs a DocTypeNode instance containing the specified Node and flag.
         * 
         * @param newNode The unprocessed document type.
         * @param newFlag An indicator of what type of document this is (true for "standard," false for "routing").
         */
        public DocTypeNode(Node newNode, boolean newFlag) {
            docNode = newNode;
            isStandard = newFlag;
        }
    }

}