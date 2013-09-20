package com.moovt;

import org.codehaus.groovy.transform.GroovyASTTransformationClass
import com.moovt.LoggingASTTransformation
import java.lang.annotation.*

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass(["com.moovt.LoggingASTTransformation"])
public @interface WithLogging {
}