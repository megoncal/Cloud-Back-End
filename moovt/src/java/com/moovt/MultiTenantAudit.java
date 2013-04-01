package com.moovt;


import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import java.lang.annotation.*;

/**
 * This is the interface that register the <code>MultiTenantAudit</code>AST Transformation used in all domain classes
 *
 * @author egoncalves
 *
 */@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass({"com.moovt.MultiTenantAuditASTTransformation"})
public @interface MultiTenantAudit {
}