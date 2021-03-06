/*
 * Copyright 2015 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.servlet.filter;

import com.stormpath.sdk.lang.Strings;
import com.stormpath.sdk.servlet.http.UserAgent;
import com.stormpath.sdk.servlet.http.impl.DefaultUserAgent;
import com.stormpath.sdk.servlet.util.ServletUtils;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @since 1.0.RC3
 */
public class LogoutFilter extends HttpFilter {

    protected String getLogoutNextUrl() {
        return getConfig().getLogoutNextUrl();
    }

    @Override
    protected void filter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws Exception {

        //clear out any authentication/account state:
        request.logout();

        //it is a security risk to not terminate a session (if one exists) on logout:
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String next = request.getParameter("next");

        if (!Strings.hasText(next)) {
            next = getLogoutNextUrl();
        }

        if (isHtmlPreferred(request)) {
            ServletUtils.issueRedirect(request, response, next, null, true, true);
        } else {
            //probably an ajax or non-browser client - return 200 ok:
            response.setStatus(HttpServletResponse.SC_OK);
        }

        //don't continue the chain - we want to short circuit and redirect
    }

    protected boolean isHtmlPreferred(HttpServletRequest request) {
        UserAgent ua = getUserAgent(request);
        return ua.isHtmlPreferred();
    }

    protected UserAgent getUserAgent(HttpServletRequest request) {
        return new DefaultUserAgent(request);
    }
}
