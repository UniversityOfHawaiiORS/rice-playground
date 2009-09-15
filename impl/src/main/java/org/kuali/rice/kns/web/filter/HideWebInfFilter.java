/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.kns.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple filter that 404s any urls to embedded module WEB-INF directories.
 * Another solution would be for the container to disable directory browsing, however
 * files may still be accessed directly.  This filter will pre-emptively catch the URL
 * which means that application code cannot actually handle those URLs (for instance,
 * to do its own error handling).
 *
 * There is probably a better way to do this, e.g. a filter to bean proxy in some spring context,
 * but the sample app doesn't really have a web context of its own to put this in.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class HideWebInfFilter implements Filter {

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // nothing
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
        if ((req instanceof HttpServletRequest)) { 

            HttpServletRequest hsr = (HttpServletRequest) req;
    
            if (hsr.getRequestURI().matches(".*/?WEB-INF/?.*")) {
                HttpServletResponse hsresp = (HttpServletResponse) res;
                hsresp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        fc.doFilter(req, res);
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig arg0) throws ServletException {
        // nada
    }
}
