package com.cyberway.spring_boot_starter_cqrs.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ScopedProxyMode;

import com.cyberway.spring_boot_starter_cqrs.anotation.DomainEventListener;
import com.cyberway.spring_boot_starter_cqrs.domain.EventListenerHandle;
import com.cyberway.spring_boot_starter_cqrs.util.MyMessageListenerAdapter;

@Configurable
@AutoConfigureAfter(CQRStarterAutoConfiguration.class)
@ComponentScan(value = "com.cyberway.spring_boot_starter_cqrs.anotation", scopedProxy = ScopedProxyMode.NO)
public class MqAutoConfiguration implements ApplicationContextAware {

	@Autowired
	List<String> eventSources;

	@Autowired
	List<EventListenerHandle> eventHandlers;

	private List<Queue> queues;
	private ApplicationContext applicationContext;

	private final String LISTENER_CONTAINER_RESOURCE_NAME = "listenerContainer";

	private DirectExchange domainEventExchange;
	public static final String EXCHANGE_COMMON_EVENT = "exchange.common.event";

	@Bean
	DirectExchange domainEventExchange() {
		DirectExchange directExchange = new DirectExchange(EXCHANGE_COMMON_EVENT);
		this.domainEventExchange = directExchange;
		return directExchange;

	}

	@Bean(name = "domainEventQueues")
	List<Queue> queues() {
		List<Queue> queues = new ArrayList<>();

		if (eventSources != null && !eventSources.isEmpty()) {
			// TODO pass other properties
			for (String queue : eventSources) {
				Queue q = new Queue(queue);
				queues.add(q);
			}
		}
		this.queues = queues;
		return queues;
	}

	@Bean
	List<Binding> domainEventsBinds(ConnectionFactory connectionFactory)
			throws InstantiationException, IllegalAccessException {
		List<Binding> binds = new ArrayList<>(this.queues.size());

		for (Queue q : this.queues) {
			// FIXME routingkey too long
			Binding bind = BindingBuilder.bind(q).to(domainEventExchange).withQueueName();
			binds.add(bind);
		}
		this.setUpmessageListenerContainer(connectionFactory);
		return binds;
	}

	public void setUpmessageListenerContainer(ConnectionFactory connectionFactory)
			throws InstantiationException, IllegalAccessException {

		List<SimpleMessageListenerContainer> containers = new ArrayList<>();

		ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) this.applicationContext;

		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext
				.getBeanFactory();

		int i = 0;
		for (EventListenerHandle eh : this.eventHandlers) {
			SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
			container.setConnectionFactory(connectionFactory);

			MyMessageListenerAdapter adapter = new MyMessageListenerAdapter(eh.getClazz().newInstance());

			Map<String, String> queueOrTagToMethodName = new HashMap<>();

			if (eh.getMethods() != null && !eh.getMethods().isEmpty()) {

				List<String> queueNames = eh.getMethods().stream().map(e -> {

					queueOrTagToMethodName.put(e.getQueue(), e.getMethod());

					return e.getQueue();

				}).collect(Collectors.toList());

				container.setQueueNames(queueNames.toArray(new String[queueNames.size()]));

				adapter.setQueueOrTagToMethodName(queueOrTagToMethodName);

			}
			Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter();
			jsonMessageConverter.setClassMapper(new ClassMapper() {

				@Override
				public Class<?> toClass(MessageProperties properties) {
					return eh.getClazz().getAnnotation(DomainEventListener.class).value();
				}

				@Override
				public void fromClass(Class<?> clazz, MessageProperties properties) {

				}
			});
			adapter.setMessageConverter(jsonMessageConverter);
			container.setMessageListener(adapter);
			containers.add(container);
			defaultListableBeanFactory.registerSingleton(this.LISTENER_CONTAINER_RESOURCE_NAME + i, container);
			i++;

		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
