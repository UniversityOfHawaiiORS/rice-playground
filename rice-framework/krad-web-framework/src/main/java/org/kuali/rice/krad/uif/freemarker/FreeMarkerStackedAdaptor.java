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
package org.kuali.rice.krad.uif.freemarker;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.layout.StackedLayoutManager;

import freemarker.core.Environment;
import freemarker.core.InlineTemplateAdaptor;
import freemarker.template.TemplateException;

/**
 * Inline FreeMarker template adaptor for supporting stacked.ftl 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class FreeMarkerStackedAdaptor implements InlineTemplateAdaptor, Serializable {

    private static final long serialVersionUID = -4442716566711789593L;

    /**
     * Render a KRAD collection via the stacked layout manager inline.
     * 
     * {@inheritDoc}
     */
    @Override
    public void accept(Environment env) throws TemplateException, IOException {
        @SuppressWarnings("unchecked")
        List<? extends Component> items = FreeMarkerInlineRenderUtils.resolve(env, "items", List.class);
        StackedLayoutManager manager = FreeMarkerInlineRenderUtils.resolve(env, "manager", StackedLayoutManager.class);
        CollectionGroup container = FreeMarkerInlineRenderUtils.resolve(env, "container", CollectionGroup.class);
        FreeMarkerInlineRenderUtils.renderStacked(env, items, manager, container);
    }

}
