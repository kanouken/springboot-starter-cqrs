package com.cyberway.spring_boot_starter_cqrs.anotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import com.cyberway.msf.commons.base.util.JsonUtils;
import com.cyberway.spring_boot_starter_cqrs.config.MqAutoConfiguration;

/**
 * @author xnengquan
 * @since 2018/12/29
 */
@Aspect
@EnableAspectJAutoProxy(proxyTargetClass = false)
@Component
public class EventSourceAspect {

	@Autowired
	private AmqpTemplate rabbitTemplate;

	/**
	 * 在被注解了 EventSourcing 的方法上拦截
	 */
	@Pointcut("execution(@com.cyberway.spring_boot_starter_cqrs.anotation.EventSourcing * *(..))")
	public void eventSourcing() {

	}

	@AfterReturning(pointcut = "eventSourcing() && @annotation(eventSourcing)", returning = "ret")
	public void doAfterReturning(JoinPoint joinPoint, EventSourcing eventSourcing, Object ret) {

		String methodName = joinPoint.getSignature().getName();

		String eventName = eventSourcing.value();

		DomainEventSourcing annotation = joinPoint.getTarget().getClass().getAnnotation(DomainEventSourcing.class);

		String routingKey = "cqrs_" + annotation.value().getName() + "_";
		if (eventName == null || eventName.trim().equals("")) {
			routingKey += methodName;
		} else {
			routingKey += eventName;
		}

		if (null != ret) {

			Message message = MessageBuilder.withBody(JsonUtils.toJson(ret).getBytes())
					.setContentType(MessageProperties.CONTENT_TYPE_JSON).build();
			this.rabbitTemplate.convertAndSend(MqAutoConfiguration.EXCHANGE_COMMON_EVENT, routingKey, message);
		}

	}

}
