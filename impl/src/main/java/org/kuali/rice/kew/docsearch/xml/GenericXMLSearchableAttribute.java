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
package org.kuali.rice.kew.docsearch.xml;

import java.util.Map;

import org.kuali.rice.kew.docsearch.SearchableAttribute;
import org.kuali.rice.kew.rule.bo.RuleAttribute;


/**
 * Interface representing attributes that are backed by xml.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public interface GenericXMLSearchableAttribute extends SearchableAttribute {

	public void setRuleAttribute(RuleAttribute ruleAttribute);
	public void setParamMap(Map paramMap);
	public Map getParamMap();
}
