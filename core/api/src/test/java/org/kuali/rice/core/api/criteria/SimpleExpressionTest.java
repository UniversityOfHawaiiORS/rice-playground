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
package org.kuali.rice.core.api.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.junit.Test;

/**
 * A test for the {@link SimpleExpression} abstract base class. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class SimpleExpressionTest {
    
    /**
     * Test method for {@link org.kuali.rice.core.api.criteria.SimpleExpression#SimpleExpression()}.
     * 
     * This empty constructor should only be invoked by JAXB.  We will invoke to ensure that it doesn't raise an exception.
     */
    @Test
    public void testSimpleExpression() {
        StringOnlyExpression expression1 = new StringOnlyExpression();
        assertNull(expression1.getPropertyPath());
        assertNull(expression1.getValue());
        
        new AllExpression();
    }

    /**
     * Test method for {@link org.kuali.rice.core.api.criteria.SimpleExpression#SimpleExpression(java.lang.String, org.kuali.rice.core.api.criteria.CriteriaValue)}.
     */
    @Test
    public void testSimpleExpressionStringCriteriaValueOfObject() {

        StringOnlyExpression expression2 = new StringOnlyExpression("path", new CriteriaStringValue("pathValue"));
        assertEquals("path", expression2.getPropertyPath());
        assertTrue(expression2.getValue() instanceof CriteriaStringValue);
        assertEquals("pathValue", expression2.getValue().getValue());
        
        // let's ensure that the "supports" method is being called, try to pass a CriteriaDateTimeValue which should trigger IllegalArgumentException
        try {
            new StringOnlyExpression("path", new CriteriaDateTimeValue(Calendar.getInstance()));
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }
        
        // check the failure cases
        try {
            new StringOnlyExpression(null, null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }
        
        try {
            new StringOnlyExpression(null, new CriteriaStringValue("pathValue"));
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }
        
        // use a "blank" string for propertyPath, should not be allowed
        try {    
            new StringOnlyExpression(" ", new CriteriaStringValue("pathValue"));
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }
        
        try {
            new StringOnlyExpression("path", null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }
        
        AllExpression expression3 = new AllExpression("path", new CriteriaDecimalValue(BigDecimal.ZERO));
        assertEquals("path", expression3.getPropertyPath());
        assertTrue(expression3.getValue() instanceof CriteriaDecimalValue);
        assertEquals(BigDecimal.ZERO, expression3.getValue().getValue());
    }

    /**
     * Test method for {@link org.kuali.rice.core.api.criteria.SimpleExpression#supportsCriteriaValue(java.lang.Class, org.kuali.rice.core.api.criteria.CriteriaValue)}.
     */
    @Test
    public void testSupportsCriteriaValue() {
        
        // first test failure cases
        
        try {
            CriteriaSupportUtils.supportsCriteriaValue(null, null);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
        
        try {
        	CriteriaSupportUtils.supportsCriteriaValue(null, new CriteriaStringValue("value"));
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
        
        try {
        	CriteriaSupportUtils.supportsCriteriaValue(SimpleExpression.class, null);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
        
        // SimpleExpression supports string, decimal, integer and dateTime CriteriaValues
        assertTrue("Should support CriteriaStringValue", CriteriaSupportUtils.supportsCriteriaValue(AllExpression.class, new CriteriaStringValue("value")));
        assertTrue("Should support CriteriaDecimalValue", CriteriaSupportUtils.supportsCriteriaValue(AllExpression.class, new CriteriaDecimalValue(BigDecimal.ZERO)));
        assertTrue("Should support CriteriaIntegerValue", CriteriaSupportUtils.supportsCriteriaValue(AllExpression.class, new CriteriaIntegerValue(BigInteger.ZERO)));
        assertTrue("Should support CriteriaDateTimeValue", CriteriaSupportUtils.supportsCriteriaValue(AllExpression.class, new CriteriaDateTimeValue(Calendar.getInstance())));
        
        // test an expression which only supports string criteria
        assertTrue("Should support CriteriaStringValue", CriteriaSupportUtils.supportsCriteriaValue(StringOnlyExpression.class, new CriteriaStringValue("value")));
        assertFalse("Should NOT support CriteriaDecimalValue", CriteriaSupportUtils.supportsCriteriaValue(StringOnlyExpression.class, new CriteriaDecimalValue(BigDecimal.ZERO))); 
        
    }
    
    /**
     * A mock SimpleExpression for use in the unit test which allows all of the different {@link CriteriaValue}.
     * 
     * @author Kuali Rice Team (rice.collab@kuali.org)
     */
    @XmlRootElement(name = LikeExpression.Constants.ROOT_ELEMENT_NAME)
    @XmlAccessorType(XmlAccessType.NONE)
    @XmlType(name = "AllExpressionType")
    private static final class AllExpression extends AbstractExpression implements SimpleExpression {

    	private static final long serialVersionUID = -5606375770690671272L;
    	
		@XmlAttribute(name = SimpleExpression.PROPERTY_PATH)
    	private final String propertyPath;
    	@XmlElements(value = {
        		@XmlElement(name = CriteriaStringValue.Constants.ROOT_ELEMENT_NAME, type = CriteriaStringValue.class, required = true),
        		@XmlElement(name = CriteriaDateTimeValue.Constants.ROOT_ELEMENT_NAME, type = CriteriaDateTimeValue.class, required = true),
        		@XmlElement(name = CriteriaDecimalValue.Constants.ROOT_ELEMENT_NAME, type = CriteriaDecimalValue.class, required = true),
        		@XmlElement(name = CriteriaIntegerValue.Constants.ROOT_ELEMENT_NAME, type = CriteriaIntegerValue.class, required = true)
        })
    	private final CriteriaValue<?> value;
    	
        private AllExpression() {
            this.propertyPath = null;
            this.value = null;
        }
        
        public AllExpression(String propertyPath, CriteriaValue<?> value) {
        	CriteriaSupportUtils.validateSimpleExpressionConstruction(getClass(), propertyPath, value);
    		this.propertyPath = propertyPath;
    		this.value = value;
        }
        
        @Override
        public String getPropertyPath() {
        	return propertyPath;
        }
        
    	@Override
    	public CriteriaValue<?> getValue() {
    		return value;
    	}
                
    }
    
    /**
     * A mock SimpleExpression for use in the unit test which only allows StringCriteriaValue.
     * 
     * @author Kuali Rice Team (rice.collab@kuali.org)
     */
    @XmlRootElement(name = LikeExpression.Constants.ROOT_ELEMENT_NAME)
    @XmlAccessorType(XmlAccessType.NONE)
    @XmlType(name = "StringOnlyExpressionType")
    private static final class StringOnlyExpression extends AbstractExpression implements SimpleExpression {

    	private static final long serialVersionUID = 5874946840884110187L;
    	
		@XmlAttribute(name = SimpleExpression.PROPERTY_PATH)
    	private final String propertyPath;
    	@XmlElements(value = {
        		@XmlElement(name = CriteriaStringValue.Constants.ROOT_ELEMENT_NAME, type = CriteriaStringValue.class, required = true)
        })
    	private final CriteriaValue<?> value;
    	
        private StringOnlyExpression() {
            this.propertyPath = null;
            this.value = null;
        }
        
        public StringOnlyExpression(String propertyPath, CriteriaValue<?> value) {
        	CriteriaSupportUtils.validateSimpleExpressionConstruction(getClass(), propertyPath, value);
    		this.propertyPath = propertyPath;
    		this.value = value;
        }
        
        @Override
        public String getPropertyPath() {
        	return propertyPath;
        }
        
    	@Override
    	public CriteriaValue<?> getValue() {
    		return value;
    	}
                
    }

}
