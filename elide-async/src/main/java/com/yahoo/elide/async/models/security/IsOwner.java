package com.yahoo.elide.async.models.security;

import java.security.Principal;
import java.util.Optional;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.async.models.AsyncQuery;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.checks.OperationCheck;

public class IsOwner {
	@SecurityCheck(AsyncQueryOwner.PRINCIPAL_IS_OWNER)
    public static class AsyncQueryOwner extends OperationCheck<AsyncQuery> {
		
		public static final String PRINCIPAL_IS_OWNER = "Principal is Owner";
       
		@Override
		public boolean ok(AsyncQuery object, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
			Principal principal = ((Principal) requestScope.getUser().getOpaqueUser());

            return object.getPrincipalName().equals(principal.getName());
		}
    }
    
	/*@SecurityCheck(AsyncQueryResultOwner.PRINCIPAL_IS_OWNER)
    public static class AsyncQueryResultOwner extends OperationCheck<AsyncQueryResult> {
		
		public static final String PRINCIPAL_IS_OWNER = "Principal is Owner";
        
		@Override
		public boolean ok(AsyncQueryResult object, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
			Principal principal = ((Principal) requestScope.getUser().getOpaqueUser());

            return object.getPrincipalName().equals(principal.getName());
		}
    }*/
}