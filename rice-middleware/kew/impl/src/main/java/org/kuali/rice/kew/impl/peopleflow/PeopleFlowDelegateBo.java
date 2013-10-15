/*
 * Copyright 2006-2013 The Kuali Foundation
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

package org.kuali.rice.kew.impl.peopleflow;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.delegation.DelegationType;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.kew.api.action.ActionRequestPolicy;
import org.kuali.rice.kew.api.peopleflow.PeopleFlowDelegate;
import org.kuali.rice.kew.api.peopleflow.PeopleFlowDelegateContract;
import org.kuali.rice.kim.api.group.GroupContract;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.role.RoleContract;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.Serializable;

@Entity
@Table(name="KREW_PPL_FLW_DLGT_T")
public class PeopleFlowDelegateBo implements Serializable, PeopleFlowDelegateContract {

    @Id
    @Column(name="PPL_FLW_DLGT_ID")
    private String id;

    @Column(name="PPL_FLW_MBR_ID")
    private String peopleFlowMemberId;

    @Column(name="MBR_ID")
    private String memberId;

    @Column(name="MBR_TYP_CD")
    private String memberTypeCode;

    @Column(name="ACTN_RQST_PLCY_CD")
    private String actionRequestPolicyCode;

    @Column(name="DLGN_TYP_CD")
    private String delegationTypeCode;

    @Column(name="RSP_ID")
    private String responsibilityId;

    @Version
    @Column(name="VER_NBR")
    private String versionNumber;

    // not-persisted
    @Transient
    private String memberName;

    @Transient
    private Person person;

    @Transient
    private GroupContract group;

    @Transient
    private RoleContract role;

    public static PeopleFlowDelegate to(PeopleFlowDelegateBo delegateBo) {
        if (delegateBo == null) {
            return null;
        }
        PeopleFlowDelegate.Builder builder = PeopleFlowDelegate.Builder.create(delegateBo);

        return builder.build();
    }

    public static PeopleFlowDelegateBo from(PeopleFlowDelegate delegate) {

        if (delegate == null) {
            return null;
        }
        PeopleFlowDelegateBo delegateBo = new PeopleFlowDelegateBo();
        delegateBo.setMemberId(delegate.getMemberId());
        delegateBo.setMemberType(delegate.getMemberType());
        if (delegate.getActionRequestPolicy() != null) {
            delegateBo.setActionRequestPolicyCode(delegate.getActionRequestPolicy().getCode());
        }
        if (delegate.getDelegationType() != null) {
            delegateBo.setDelegationTypeCode(delegate.getDelegationType().getCode());
        }
        delegateBo.setResponsibilityId(delegate.getResponsibilityId());
        return delegateBo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeopleFlowMemberId() {
        return peopleFlowMemberId;
    }

    public void setPeopleFlowMemberId(String peopleFlowMemberId) {
        this.peopleFlowMemberId = peopleFlowMemberId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberTypeCode() {
        return memberTypeCode;
    }

    public void setMemberTypeCode(String memberTypeCode) {
        this.memberTypeCode = memberTypeCode;
    }

    public String getActionRequestPolicyCode() {
        return actionRequestPolicyCode;
    }

    public void setActionRequestPolicyCode(String actionRequestPolicyCode) {
        this.actionRequestPolicyCode = actionRequestPolicyCode;
    }

    public String getDelegationTypeCode() {
        return delegationTypeCode;
    }

    public void setDelegationTypeCode(String delegationTypeCode) {
        this.delegationTypeCode = delegationTypeCode;
    }

    public String getResponsibilityId() {
        return responsibilityId;
    }

    public void setResponsibilityId(String responsibilityId) {
        this.responsibilityId = responsibilityId;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setGroup(GroupContract group) {
        this.group = group;
    }

    public void setRole(RoleContract role) {
        this.role = role;
    }

    public Person getPerson() {
        if (MemberType.PRINCIPAL.getCode().equals(memberTypeCode)) {
            if ((this.person == null) || !person.getPrincipalId().equals(memberId) || !person.getPrincipalName().equals(memberName)) {
                // use member name first
                if (StringUtils.isNotBlank(memberName)) {
                    this.person = KimApiServiceLocator.getPersonService().getPersonByPrincipalName(memberName);
                } else {
                    this.person = KimApiServiceLocator.getPersonService().getPerson(memberId);
                }
            }

            if (this.person != null) {
                memberId = person.getPrincipalId();
                memberName = person.getPrincipalName();

                return this.person;
            }
        }

        Person newPerson = null;
        // TODO - figure out a better way to do this.
        try {
            newPerson = KimApiServiceLocator.getPersonService().getPersonImplementationClass().newInstance();
        } catch(InstantiationException ie) {
            throw new IllegalStateException(ie);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        }

        return newPerson;
    }

    public GroupContract getGroup() {
        if (MemberType.GROUP.getCode().equals(memberTypeCode)) {
              KimApiServiceLocator.getGroupService().getGroup(this.memberId);
            if (this.group != null) {
                this.memberId = this.group.getId();
                this.memberName = this.group.getNamespaceCode() + " : " + group.getName();
            }
        }

        return this.group;
    }

    public RoleContract getRole() {
        if (MemberType.ROLE.getCode().equals(memberTypeCode)) {
            KimApiServiceLocator.getRoleService().getRole(this.memberId);
            if (this.role != null) {
                this.memberId = this.role.getId();
                memberName = role.getNamespaceCode() + " : " + role.getName();
            }
        }

        return this.role;
    }

    public void setMemberName(String memberName) throws InstantiationException, IllegalAccessException {
        this.memberName = memberName;

        // trigger update of related object (only person can be updated by name)
        if (MemberType.PRINCIPAL.getCode().equals(memberTypeCode)) {
            getPerson();
        }
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
        updateRelatedObject();
    }

    // trigger update of related object
    public void updateRelatedObject() {
        if (MemberType.PRINCIPAL.getCode().equals(memberTypeCode)) {
            getPerson();
        } else if (MemberType.GROUP.getCode().equals(memberTypeCode)) {
            getGroup();
        } else if (MemberType.ROLE.getCode().equals(memberTypeCode)) {
            getRole();
        }
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.fromCode(memberTypeCode);
    }

    public void setMemberType(MemberType type) {
        memberTypeCode = type.getCode();
    }

    @Override
    public ActionRequestPolicy getActionRequestPolicy() {
        return ActionRequestPolicy.fromCode(actionRequestPolicyCode);
    }

    @Override
    public DelegationType getDelegationType() {
        return DelegationType.fromCode(delegationTypeCode);
    }

    public void setDelegationType(DelegationType delegationType) {
        this.delegationTypeCode = delegationType.getCode();
    }
}
