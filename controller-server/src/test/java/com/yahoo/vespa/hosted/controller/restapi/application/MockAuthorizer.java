// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.controller.restapi.application;

import com.yahoo.container.jdisc.HttpRequest;
import com.yahoo.vespa.hosted.controller.Controller;
import com.yahoo.vespa.hosted.controller.TestIdentities;
import com.yahoo.vespa.hosted.controller.api.identifiers.AthensDomain;
import com.yahoo.vespa.hosted.controller.api.identifiers.UserId;
import com.yahoo.vespa.hosted.controller.api.integration.athens.AthensPrincipal;
import com.yahoo.vespa.hosted.controller.api.integration.athens.NToken;
import com.yahoo.vespa.hosted.controller.api.integration.entity.EntityService;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Optional;

/**
 * This overrides methods in Authorizer which relies on properties set by jdisc HTTP filters.
 * This is necessary because filters are not currently executed when executing requests with Application.
 * 
 * @author bratseth
 */
@SuppressWarnings("unused") // injected
public class MockAuthorizer extends Authorizer {

    public MockAuthorizer(Controller controller, EntityService entityService) {
        super(controller, entityService);
    }

    /** Returns a principal given by the request parameters 'domain' and 'user' */
    @Override
    public Optional<Principal> getPrincipalIfAny(HttpRequest request) {
        if (request.getProperty("user") == null) return Optional.empty();
        return Optional.of(new AthensPrincipal(new AthensDomain(request.getProperty("domain")),
                                               new UserId(request.getProperty("user"))));
    }

    /** Returns the hardcoded NToken of {@link TestIdentities#userId} */
    @Override
    public Optional<NToken> getNToken(HttpRequest request) {
        return Optional.of(TestIdentities.userNToken);
    }

    private static class MockPrincipal implements Principal {

        @Override
        public String getName() { return TestIdentities.userId.id(); }

    }

    @Override
    protected Optional<SecurityContext> securityContextOf(HttpRequest request) {
        return getPrincipalIfAny(request).map(MockSecurityContext::new);
    }
    
    private static final class MockSecurityContext implements SecurityContext {
        
        private final Principal principal;
        
        private MockSecurityContext(Principal principal) {
            this.principal = principal;
        }
        
        @Override
        public Principal getUserPrincipal() { return principal; }

        @Override
        public boolean isUserInRole(String role) { return false; }

        @Override
        public boolean isSecure() { return true; }

        @Override
        public String getAuthenticationScheme() { throw new UnsupportedOperationException(); }

    }

}
