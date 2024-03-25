package com.company.jmixpm.security.specific;

import io.jmix.core.accesscontext.SpecificOperationAccessContext;

public class JmixPmProjectArchiveContext extends SpecificOperationAccessContext {

    public static final String NAME = "pm.projects.archive";

    public JmixPmProjectArchiveContext() {
        super(NAME);
    }
}
