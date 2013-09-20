package com.moovt.audit;


import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.*;

/**
 * This is the interface that register the <code>MultiTenantAudit</code>AST Transformation used in all domain classes
 *
 * @author egoncalves
 *
 */

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["com.moovt.audit.MultiTenantAuditASTTransformation"])
public @interface MultiTenantAudit {
}