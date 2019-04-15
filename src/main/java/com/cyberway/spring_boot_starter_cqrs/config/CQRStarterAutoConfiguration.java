package com.cyberway.spring_boot_starter_cqrs.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cyberway.spring_boot_starter_cqrs.anotation.DomainEventListener;
import com.cyberway.spring_boot_starter_cqrs.anotation.DomainEventSourcing;
import com.cyberway.spring_boot_starter_cqrs.anotation.EventHandle;
import com.cyberway.spring_boot_starter_cqrs.anotation.EventSourcing;
import com.cyberway.spring_boot_starter_cqrs.domain.EventHandleMethod;
import com.cyberway.spring_boot_starter_cqrs.domain.EventListenerHandle;
import com.cyberway.spring_boot_starter_cqrs.properties.CqrsProperties;
import com.cyberway.spring_boot_starter_cqrs.util.AopTargetUtils;

@Configuration
@EnableConfigurationProperties(value = CqrsProperties.class)
@AutoConfigureBefore(MqAutoConfiguration.class)
public class CQRStarterAutoConfiguration implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean
	public List<String> eventSources() throws Exception {

		List<String> channels = new ArrayList<>();
		Map<String, Object> beansWithAnnotationMap = this.applicationContext
				.getBeansWithAnnotation(DomainEventSourcing.class);
		Class<? extends Object> clazz = null;
		for (Map.Entry<String, Object> entry : beansWithAnnotationMap.entrySet()) {
			clazz = AopTargetUtils.getTarget(entry.getValue()).getClass();

			if (clazz.isAnnotationPresent(DomainEventSourcing.class)) {
				DomainEventSourcing annotation = clazz.getAnnotation(DomainEventSourcing.class);

				Class<?> value = annotation.value();

				Method[] methods = clazz.getDeclaredMethods();

				for (Method m : methods) {

					if (m.isAnnotationPresent(EventSourcing.class)) {
						String channel = "cqrs_" + value.getName() + "_";
						EventSourcing source = m.getAnnotation(EventSourcing.class);
						if (source.value() != null && !source.value().trim().equals("")) {
							channel += source.value();
						} else {
							channel += m.getName();
						}

						channels.add(channel);
					}

				}
			}
		}
		return channels;
	}

	@Bean
	List<EventListenerHandle> eventHandlers() {
		List<EventListenerHandle> eventHandles = new ArrayList<>();
		Map<String, Object> beansWithAnnotationMap = this.applicationContext
				.getBeansWithAnnotation(DomainEventListener.class);
		Class<? extends Object> clazz = null;
		for (Map.Entry<String, Object> entry : beansWithAnnotationMap.entrySet()) {
			clazz = entry.getValue().getClass();
			EventListenerHandle handle = new EventListenerHandle();
			handle.setClazz(clazz);
			if (clazz.isAnnotationPresent(DomainEventListener.class)) {
				DomainEventListener annotation = clazz.getAnnotation(DomainEventListener.class);

				Class<?> value = annotation.value();

				Method[] methods = clazz.getDeclaredMethods();
				List<EventHandleMethod> ms = new ArrayList<>();
				for (Method m : methods) {

					if (m.isAnnotationPresent(EventHandle.class)) {
						String queue = "cqrs_" + value.getName() + "_" + m.getName();
						ms.add(new EventHandleMethod(m.getName(), queue));
						handle.setMethods(ms);
					}
				}

				eventHandles.add(handle);
			}
		}

		return eventHandles;

	}

}