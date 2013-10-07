/**
 * Copyright 2005-2013 The Kuali Foundation
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
package org.kuali.rice.krad.uif.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.uif.element.Message;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycle;
import org.kuali.rice.krad.uif.view.InquiryView;

/**
 * Unit tests for proving correct operation of the ViewHelperService.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ComponentFactoryTest extends ProcessLoggingUnitTest {

    @BeforeClass
    public static void setUpClass() throws Throwable {
        UifUnitTestUtils.establishMockConfig("KRAD-ComponentFactoryTest");
    }

    @AfterClass
    public static void tearDownClass() throws Throwable {
        GlobalResourceLoader.stop();
    }

    @Test
    public void testSanity() throws Throwable {
        Message message = ViewLifecycle.encapsulateInitialization(new Callable<Message>() {
            @Override
            public Message call() throws Exception {
                return ComponentFactory.getMessage();
            }
        });
        assertEquals("uif-message", message.getCssClasses().get(0));
    }

    @Test
    public void testInquiry() throws Throwable {
        InquiryView inquiryView = ViewLifecycle.encapsulateInitialization(new Callable<InquiryView>() {
            @Override
            public InquiryView call() throws Exception {
                return ComponentFactory.getInquiryView();
            }
        });
        assertEquals("uif-formView", inquiryView.getCssClasses().get(0));
    }

    @Test
    public void testStrict() throws Throwable {
        try {
            ComponentFactory.getMessage();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // OK
        }
    }

}
