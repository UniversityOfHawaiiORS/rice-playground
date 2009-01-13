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
package org.kuali.rice.kns.bo.options;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kns.bo.Country;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.web.ui.KeyLabelPair;

/**
 * This class returns list of country value pairs.
 */
public class CountryValuesFinder extends KeyValuesBase {

	static List<Country> boList;
	static Country defaultCountry;
	
    /*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyLabelPair> getKeyValues() {
    	if ( boList == null ) {
    		boList = KNSServiceLocator.getCountryService().findAllCountries();
    		defaultCountry = KNSServiceLocator.getCountryService().getDefaultCountry();
    	}
        List<KeyLabelPair> keyValues = new ArrayList<KeyLabelPair>();

        for (Country element : boList) {
            
            // Find default country code and pull it out so we can set it first in the results list later.
            if (!StringUtils.equals(defaultCountry.getPostalCountryCode(), element.getPostalCountryCode())) {
                if(element.isActive()) {
                    keyValues.add(new KeyLabelPair(element.getPostalCountryCode(), element.getPostalCountryName()));
                }
            }
        }

        List<KeyLabelPair> keyValueUSFirst = new ArrayList<KeyLabelPair>();
        keyValueUSFirst.add(new KeyLabelPair("", ""));
        keyValueUSFirst.add(new KeyLabelPair(defaultCountry.getPostalCountryCode(), defaultCountry.getPostalCountryName()));
        keyValueUSFirst.addAll(keyValues);

        return keyValueUSFirst;
    }
}
