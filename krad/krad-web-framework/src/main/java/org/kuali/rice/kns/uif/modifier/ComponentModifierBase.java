/*
 * Copyright 2011 The Kuali Foundation
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
package org.kuali.rice.kns.uif.modifier;

import org.kuali.rice.kns.uif.UifConstants;

/**
 * Base class for <code>ComponentModifier</code> implementations
 * 
 * <p>
 * Holds run phase property and defaults to the INITIALIZE phase, and the order
 * property for setting the order in which the component modifier will be
 * invoked
 * </p>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class ComponentModifierBase implements ComponentModifier {
	private static final long serialVersionUID = -8284332412469942130L;

	private String runPhase;
	private String runCondition;
	private int order;

	public ComponentModifierBase() {
		runPhase = UifConstants.ViewPhases.INITIALIZE;
		order = 0;
	}

	/**
	 * @see org.kuali.rice.kns.uif.modifier.ComponentModifier#getRunPhase()
	 */
	public String getRunPhase() {
		return this.runPhase;
	}

	/**
	 * Setter for the component initializer run phase
	 * 
	 * @param runPhase
	 */
	public void setRunPhase(String runPhase) {
		this.runPhase = runPhase;
	}

	/**
	 * @see org.kuali.rice.kns.uif.modifier.ComponentModifier#getRunCondition()
	 */
	public String getRunCondition() {
		return this.runCondition;
	}

	/**
	 * Setter for the component modifiers run condition
	 * 
	 * @param runCondition
	 */
	public void setRunCondition(String runCondition) {
		this.runCondition = runCondition;
	}

	/**
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public int getOrder() {
		return this.order;
	}

	/**
	 * @see org.kuali.rice.kns.uif.core.Ordered#setOrder(int)
	 */
	public void setOrder(int order) {
		this.order = order;
	}

}
