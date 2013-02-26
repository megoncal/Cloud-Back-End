package com.moovt;


import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import java.lang.annotation.*;

//ElementType.TYPE is same as Class
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass({"com.moovt.MultiTenantAuditASTTransformation"})
public @interface MultiTenantAudit {
}