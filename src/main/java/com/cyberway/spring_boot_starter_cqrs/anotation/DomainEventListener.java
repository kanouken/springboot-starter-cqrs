package com.cyberway.spring_boot_starter_cqrs.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import com.cyberway.spring_boot_starter_cqrs.config.CQRStarterAutoConfiguration;

/**
 * Indicates that an annotated class is a "Domain event Listener". Such classes
 * are considered as listener handler for auto-detection
 * 
 * 
 * @author xnq
 * @see CQRStarterAutoConfiguration
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface DomainEventListener {

	Class<?> value();

}
