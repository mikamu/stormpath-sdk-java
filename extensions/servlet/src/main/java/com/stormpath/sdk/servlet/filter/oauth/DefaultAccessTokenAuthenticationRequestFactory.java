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
package com.stormpath.sdk.servlet.filter.oauth;

import com.stormpath.sdk.authc.AuthenticationRequest;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.lang.Strings;
import com.stormpath.sdk.servlet.filter.UsernamePasswordRequestFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0.RC3
 */
public class DefaultAccessTokenAuthenticationRequestFactory implements AccessTokenAuthenticationRequestFactory {

    protected static final String GRANT_TYPE_PARAM_NAME = "grant_type";

    private UsernamePasswordRequestFactory usernamePasswordRequestFactory;

    public DefaultAccessTokenAuthenticationRequestFactory(UsernamePasswordRequestFactory factory) {
        Assert.notNull(factory, "UsernamePasswordRequestFactory cannot be null.");
        this.usernamePasswordRequestFactory = factory;
    }

    public UsernamePasswordRequestFactory getUsernamePasswordRequestFactory() {
        return usernamePasswordRequestFactory;
    }

    @Override
    public AuthenticationRequest createAccessTokenAuthenticationRequest(HttpServletRequest request)
        throws OauthException {

        String grantType = Strings.clean(request.getParameter(GRANT_TYPE_PARAM_NAME));
        //this is asserted in the AccessTokenFilter so it should never be null/empty here:
        Assert.hasText(grantType, "grant_type must not be null or empty.");

        if ("password".equals(grantType)) {
            return createUsernamePasswordRequest(request);
        }

        throw new OauthException(OauthErrorCode.UNSUPPORTED_GRANT_TYPE);
    }

    protected AuthenticationRequest createUsernamePasswordRequest(HttpServletRequest request) throws OauthException {

        String username = Strings.clean(request.getParameter("username"));
        if (username == null) {
            throw new OauthException(OauthErrorCode.INVALID_REQUEST, "Missing username value.", null);
        }

        String password = Strings.clean(request.getParameter("password"));
        if (password == null) {
            throw new OauthException(OauthErrorCode.INVALID_REQUEST, "Missing password value.", null);
        }

        return getUsernamePasswordRequestFactory().createUsernamePasswordRequest(request, null, username, password);
    }
}
