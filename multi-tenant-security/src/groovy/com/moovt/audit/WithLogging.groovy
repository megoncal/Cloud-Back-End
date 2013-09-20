package com.moovt.audit;

import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.*

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass(["com.moovt.audit.LoggingASTTransformation"])
public @interface WithLogging {
}