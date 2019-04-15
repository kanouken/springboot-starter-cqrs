package com.cyberway.spring_boot_starter_cqrs.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.cyberway.spring_boot_starter_cqrs.config.CQRStarterAutoConfiguration;
import com.cyberway.spring_boot_starter_cqrs.config.MqAutoConfiguration;

/**
 * 
 * @author xnq
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ CQRStarterAutoConfiguration.class, MqAutoConfiguration.class })
public @interface EnableCQRS {

}
