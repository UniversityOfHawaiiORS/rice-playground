/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.rice.kim.lookup.valuefinder;

import org.kuali.rice.kim.util.KIMConstants;
import org.kuali.rice.kns.lookup.valueFinder.ValueFinder;

/**
 * This class is responsible for retrieving the default group type.
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class GroupTypeDefaultValueFinder implements ValueFinder {
	/**
	 * This overridden method returns the default group type value - "Default" group type.
	 * 
	 * @see org.kuali.rice.kns.lookup.valueFinder.ValueFinder#getValue()
	 */
	public String getValue() {
		return KIMConstants.GROUP_TYPE.DEFAULT_GROUP_TYPE.toString();
	}
}
