<%--
  ~ Copyright 2006-2012 The Kuali Foundation
  ~
  ~ Licensed under the Educational Community License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.opensource.org/licenses/ecl2.php
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ include file="/krad/WEB-INF/jsp/tldHeader.jsp" %>

<tiles:useAttribute name="group" classname="org.kuali.rice.krad.uif.container.DialogGroup"/>

<krad:group group="${group}">

  <krad:template component="${group.prompt}" parent="${group}"/>
  <krad:template component="${group.explanation}" parent="${group}"/>
  <krad:template component="${group.responseInputField}" parent="${group}"/>

</krad:group>

<%-- style checkboxes to look like buttons --%>
<krad:script value="styleDialogButtons();"/>
