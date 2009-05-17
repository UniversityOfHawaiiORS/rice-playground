<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp"%>

<c:set var="groupMemberAttributes" value="${DataDictionary.GroupDocumentMember.attributes}" />
<c:set var="groupQualifierAttributes" value="${DataDictionary.GroupDocumentQualifier.attributes}" />

<kul:tab tabTitle="Assignees" defaultOpen="true" tabErrorKey="document.member*">
	<div class="tab-container" align="center">
	    <kul:tableRenderPagingBanner pageNumber="${KualiForm.memberTableMetadata.viewedPageNumber}"
	                                totalPages="${KualiForm.memberTableMetadata.totalNumberOfPages}"
	                                firstDisplayedRow="${KualiForm.memberTableMetadata.firstRowIndex}" 
	                                lastDisplayedRow="${KualiForm.memberTableMetadata.lastRowIndex}"
	                                resultsActualSize="${KualiForm.memberTableMetadata.resultsActualSize}" 
	                                resultsLimitedSize="${KualiForm.memberTableMetadata.resultsLimitedSize}"
	                                buttonExtraParams=".anchor${currentTabIndex}"/>
	    <input type="hidden" name="memberTableMetadata.${Constants.TableRenderConstants.PREVIOUSLY_SORTED_COLUMN_INDEX_PARAM}" value="${KualiForm.memberTableMetadata.columnToSortIndex}"/>
	    <input type="hidden" name="memberTableMetadata.sortDescending" value="${KualiForm.memberTableMetadata.sortDescending}"/>
	    <input type="hidden" name="memberTableMetadata.viewedPageNumber" value="${KualiForm.memberTableMetadata.viewedPageNumber}"/>
	    <table cellpadding="0" cellspacing="0" summary="">
	        	<tr>
	        		<th>&nbsp;</th> 
	        		<kul:htmlAttributeHeaderCell attributeEntry="${groupMemberAttributes.memberTypeCode}" horizontal="false" />
	        		<kul:htmlAttributeHeaderCell attributeEntry="${groupMemberAttributes.memberId}" horizontal="false" />
	        		<kul:htmlAttributeHeaderCell attributeEntry="${groupMemberAttributes.memberName}" horizontal="false" />
	        		<kul:htmlAttributeHeaderCell attributeEntry="${groupMemberAttributes.activeFromDate}" horizontal="false" />
	        		<kul:htmlAttributeHeaderCell attributeEntry="${groupMemberAttributes.activeToDate}" horizontal="false" />
					<c:if test="${canAssignGroup}">	
	            		<kul:htmlAttributeHeaderCell literalLabel="Actions" scope="col" horizontal="false" />
					</c:if>	
	        	</tr>     
	          <c:if test="${canAssignGroup}">	
	             <tr>
					<th class="infoline">Add:</th>
	                <td align="left" valign="middle" class="infoline">
		                <div align="center">
		                	<input type="hidden" name="command"/>
		                	<kul:htmlControlAttribute property="member.memberTypeCode" 
		                	attributeEntry="${groupMemberAttributes.memberTypeCode}" 
		                	onchange="changeMemberTypeCode()" disabled="${readOnly}" />
							<NOSCRIPT>
		   						<html:submit value="select" alt="press this button to refresh the page after changing the delegation type." />
							</NOSCRIPT>                
			            </div>
		            	<c:set var="bo" value="${KualiForm.memberBusinessObjectName}"/>
		            	<c:set var="fc" value="${KualiForm.memberFieldConversions}"/>
					</td>
	                <td class="infoline">   
	                <div align="center">             	
						<kul:htmlControlAttribute property="member.memberId" attributeEntry="${groupMemberAttributes.memberId}" readOnly="${readOnly}"/>
						<c:if test="${!readOnly}">
			               	<kul:lookup boClassName="${bo}" fieldConversions="${fc}" anchor="${tabKey}" />
		               	</c:if>
					</div>
					</td>
	                <td class="infoline">   
	                <div align="center">             	
						<kul:htmlControlAttribute property="member.memberName" attributeEntry="${groupMemberAttributes.memberName}" readOnly="true" />
					</div>
					</td>
	                <td align="left" valign="middle" class="infoline">
	                <div align="center">
	                	<kul:htmlControlAttribute property="member.activeFromDate" attributeEntry="${groupMemberAttributes.activeFromDate}" datePicker="true" readOnly="${readOnly}" />
	                </div>
	                </td>
	                <td align="left" valign="middle" class="infoline">
	                <div align="center">
	                	<kul:htmlControlAttribute property="member.activeToDate" attributeEntry="${groupMemberAttributes.activeToDate}" datePicker="true" readOnly="${readOnly}" />
	                </div>
	                </td>
	                <td class="infoline">
       					<div align="center">
							<html:image property="methodToCall.addMember.anchor${tabKey}"
							src='${ConfigProperties.kr.externalizable.images.url}tinybutton-add1.gif' styleClass="tinybutton"/>
                        </div>
                    </td>
	    	   </tr>         
			</c:if>
			<c:if test="${KualiForm.memberTableMetadata.firstRowIndex >= 0}">
	      	<c:forEach var="member" items="${KualiForm.document.members}" varStatus="statusMember"
                 begin="${KualiForm.memberTableMetadata.firstRowIndex}" 
                 end="${KualiForm.memberTableMetadata.lastRowIndex}">
	            <tr>
					<th class="infoline" valign="top">
						<c:out value="${statusMember.index+1}" />
					</th>
		            <td align="left" valign="middle">
		               	<div align="center"> <kul:htmlControlAttribute property="document.members[${statusMember.index}].memberTypeCode"  attributeEntry="${groupMemberAttributes.memberTypeCode}" disabled="true" readOnly="false" />
						</div>
					</td>
		            <td align="left" valign="middle">
		               	<div align="center"> <kul:htmlControlAttribute property="document.members[${statusMember.index}].memberId"  attributeEntry="${groupMemberAttributes.memberId}" readOnly="${!canAssignGroup || member.edit}" />
						</div>
					</td>
		            <td align="left" valign="middle">
		               	<div align="center"> <kul:htmlControlAttribute property="document.members[${statusMember.index}].memberName"  attributeEntry="${groupMemberAttributes.memberName}" readOnly="true"  />
						</div>
					</td>
		            <td align="left" valign="middle">
		               	<div align="center"> <kul:htmlControlAttribute property="document.members[${statusMember.index}].activeFromDate"  attributeEntry="${groupMemberAttributes.activeFromDate}" readOnly="${!canAssignGroup}" datePicker="true" />
						</div>
					</td>
		            <td align="left" valign="middle">
		               	<div align="center"> <kul:htmlControlAttribute property="document.members[${statusMember.index}].activeToDate"  attributeEntry="${groupMemberAttributes.activeToDate}" readOnly="${!canAssignGroup}" datePicker="true" />
						</div>
					</td>
					<c:if test="${canAssignGroup}">
                        <td><div align="center">
                        <c:choose>
							<c:when test="${member.edit}">
	        	          		<img class='nobord' src='${ConfigProperties.kr.externalizable.images.url}tinybutton-delete2.gif' styleClass='tinybutton'/>
							</c:when>
							<c:otherwise>
                                <html:image property='methodToCall.deleteMember.line${statusMember.index}.anchor${currentTabIndex}'
                                src='${ConfigProperties.kr.externalizable.images.url}tinybutton-delete1.gif' styleClass='tinybutton'/>
							</c:otherwise>
                        </c:choose>  
                        </div></td>
                    </c:if>
				</tr>
			</c:forEach>        
			</c:if>
		</table>
	</div>
</kul:tab>