package com.company.jmixpm.security;

import io.jmix.dynattrflowui.role.DynamicAttributesRole;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(name = "CombinedManager", code = CombinedManagerRole.CODE)
public interface CombinedManagerRole extends ProjectManagementRole, DynamicAttributesRole, UiMinimalRole {
    String CODE = "combined-manager";

    @SpecificPolicy(resources = "pm.projects.archive")
    void specific();
}