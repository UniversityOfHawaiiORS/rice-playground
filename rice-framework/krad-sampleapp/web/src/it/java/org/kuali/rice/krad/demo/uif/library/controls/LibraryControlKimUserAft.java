/**
 * Copyright 2005-2014 The Kuali Foundation
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
package org.kuali.rice.krad.demo.uif.library.controls;

import org.junit.Test;

import org.kuali.rice.testtools.selenium.WebDriverLegacyITBase;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class LibraryControlKimUserAft extends WebDriverLegacyITBase {

    /**
     * /kr-krad/kradsampleapp?viewId=Demo-KIMUserControlView
     */
    public static final String BOOKMARK_URL = "/kr-krad/kradsampleapp?viewId=Demo-KIMUserControlView";

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws Exception {
        waitAndClickLibraryLink();
        waitAndClickByLinkText("Controls");
        waitAndClickByLinkText("KIM User");
    }

    protected void testControlKimUserDefault() throws Exception {
        waitAndTypeByName("testPerson.principalName","admin");
        waitAndClickByLinkText("Documentation");
        waitForTextPresent("admin, admin");
    }
    
    protected void testControlKimUserWidgetInputOnly() throws Exception {
        waitAndClickByLinkText("Widget Input Only");
        waitAndClickByXpath("//section[@id='Demo-KIMUserControl-Example2']/div/div/button");
        gotoLightBox();
        waitAndClickSearchByText();
        waitAndClickLinkContainingText("return value");
        waitForTextPresent("admin, admin");
    }
    
    @Test
    public void testControlKimUserBookmark() throws Exception {
        testControlKimUserDefault();
        testControlKimUserWidgetInputOnly();
        passed();
    }

    @Test
    public void testControlKimUserNav() throws Exception {
    	testControlKimUserDefault();
        testControlKimUserWidgetInputOnly();
        passed();
    }  
}
