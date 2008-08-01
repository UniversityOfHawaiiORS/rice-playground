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

package org.kuali.core.bo;

import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kuali.core.workflow.service.KualiWorkflowDocument;
import org.kuali.rice.kns.util.KNSPropertyConstants;


/**
 * Interface for {@link DocumentHeaderBase} 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
@Entity
@Table(name="FP_DOC_HEADER_T")
public class DocumentHeader extends PersistableBusinessObjectBase {

    @Id
	@Column(name="FDOC_NBR")
	private String documentNumber;
    @Column(name="FDOC_DESC")
	private String documentDescription;
    @Column(name="ORG_DOC_NBR")
	private String organizationDocumentNumber;
    @Column(name="FDOC_TMPL_NBR")
	private String documentTemplateNumber;
    @Column(name="FDOC_EXPLAIN_TXT")
	private String explanation;
    
    @Transient
    private KualiWorkflowDocument workflowDocument;

    /**
     * Constructor - creates empty instances of dependent objects
     * 
     */
    public DocumentHeader() {
        super();
    }

    /**
     * 
     * @return workflowDocument
     */
    public KualiWorkflowDocument getWorkflowDocument() {
        if (workflowDocument == null) {
            throw new RuntimeException("transient workflowDocument is null - this should never happen");
        }

        return workflowDocument;
    }

    /**
     * @return true if the workflowDocument is not null
     */
    public boolean hasWorkflowDocument() {
        return (workflowDocument != null);
    }


    /**
     * 
     * @param workflowDocument
     */
    public void setWorkflowDocument(KualiWorkflowDocument workflowDocument) {
        this.workflowDocument = workflowDocument;
    }

    /**
     * @return the documentNumber
     */
    public String getDocumentNumber() {
        return this.documentNumber;
    }

    /**
     * @param documentNumber the documentNumber to set
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * @return the documentDescription
     */
    public String getDocumentDescription() {
        return this.documentDescription;
    }

    /**
     * @param documentDescription the documentDescription to set
     */
    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    /**
     * @return the organizationDocumentNumber
     */
    public String getOrganizationDocumentNumber() {
        return this.organizationDocumentNumber;
    }

    /**
     * @param organizationDocumentNumber the organizationDocumentNumber to set
     */
    public void setOrganizationDocumentNumber(String organizationDocumentNumber) {
        this.organizationDocumentNumber = organizationDocumentNumber;
    }

    /**
     * @return the documentTemplateNumber
     */
    public String getDocumentTemplateNumber() {
        return this.documentTemplateNumber;
    }

    /**
     * @param documentTemplateNumber the documentTemplateNumber to set
     */
    public void setDocumentTemplateNumber(String documentTemplateNumber) {
        this.documentTemplateNumber = documentTemplateNumber;
    }

    /**
     * @see org.kuali.core.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put(KNSPropertyConstants.DOCUMENT_NUMBER, documentNumber);

        return m;
    }

    /**
     * Gets the explanation attribute. 
     * @return Returns the explanation.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Sets the explanation attribute value.
     * @param explanation The explanation to set.
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

}
