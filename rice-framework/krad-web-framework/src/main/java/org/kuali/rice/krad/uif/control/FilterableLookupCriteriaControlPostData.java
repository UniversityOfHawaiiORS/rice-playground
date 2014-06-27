/*
 * Copyright 2006-2014 The Kuali Foundation
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
package org.kuali.rice.krad.uif.control;

/**
 * Defines the post data to send for filtering search results for a filterable control.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface FilterableLookupCriteriaControlPostData {

    /**
     * Returns the class to invoke the filter search method on.
     *
     * @return the class to invoke the filter search method on
     */
    Class<? extends FilterableLookupCriteriaControl> getControlClass();

    /**
     * Returns the property name of the control that this post data is representing.
     *
     * @return the property name of the control that this post data is representing
     */
    String getPropertyName();

}