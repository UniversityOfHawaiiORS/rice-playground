/*
 * Copyright 2007-2009 The Kuali Foundation
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
package org.kuali.rice.kew.rule.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kuali.rice.core.util.KeyValue;
import org.kuali.rice.core.util.ConcreteKeyValue;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.krad.lookup.keyvalues.KeyValuesBase;

/**
 * This is a description of what this class does - ewestfal don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class RuleAttributeTypeValuesFinder extends KeyValuesBase {

	private static final List<KeyValue> RULE_ATTRIBUTE_TYPES;
	
	static {
		final List<KeyValue> ruleAttributeTypes = new ArrayList<KeyValue>();
		for (String ruleAttributeType : KEWConstants.RULE_ATTRIBUTE_TYPES) {
			ruleAttributeTypes.add(new ConcreteKeyValue(ruleAttributeType, KEWConstants.RULE_ATTRIBUTE_TYPE_MAP.get(ruleAttributeType)));
		}
		RULE_ATTRIBUTE_TYPES = Collections.unmodifiableList(ruleAttributeTypes);
	}
	
	@Override
	public List<KeyValue> getKeyValues() {
		return RULE_ATTRIBUTE_TYPES;
	}

}
