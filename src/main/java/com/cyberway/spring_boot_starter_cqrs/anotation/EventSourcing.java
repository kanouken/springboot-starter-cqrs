package com.cyberway.spring_boot_starter_cqrs.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EventSourcing {
	/**
	 * if null method name will be used
	 * 
	 * @return
	 */
	@AliasFor(value = "eventName")
	String value() default "";
}
