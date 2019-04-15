package com.cyberway.spring_boot_starter_cqrs.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.AmqpIllegalStateException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import com.cyberway.msf.commons.base.util.JsonUtils;
import com.cyberway.spring_boot_starter_cqrs.anotation.DomainEventListener;
import com.cyberway.spring_boot_starter_cqrs.anotation.EventHandle;
import com.rabbitmq.client.Channel;

@Component
public class MyMessageListenerAdapter extends MessageListenerAdapter {
	private final Map<String, Method> queueOrTagToMethod = new HashMap<String, Method>();

	public void setQueueOrTagToMethod(Map<String, Method> queueOrTagToMethodName) {
		this.queueOrTagToMethod.putAll(queueOrTagToMethodName);
	}

	protected Method getListenerMethod(Message originalMessage, Object extractedMessage) throws Exception {
		if (this.queueOrTagToMethod.size() > 0) {
			MessageProperties props = originalMessage.getMessageProperties();
			Method methodName = this.queueOrTagToMethod.get(props.getConsumerQueue());
			if (methodName == null) {
				methodName = this.queueOrTagToMethod.get(props.getConsumerTag());
			}
			if (methodName != null) {
				return methodName;
			}
		}
		return null;
	}

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		Object delegate = getDelegate();
		if (delegate != this) {
			if (delegate instanceof ChannelAwareMessageListener) {
				if (channel != null) {
					((ChannelAwareMessageListener) delegate).onMessage(message, channel);
					return;
				} else if (!(delegate instanceof MessageListener)) {
					throw new AmqpIllegalStateException("MessageListenerAdapter cannot handle a "
							+ "ChannelAwareMessageListener delegate if it hasn't been invoked with a Channel itself");
				}
			}
			if (delegate instanceof MessageListener) {
				((MessageListener) delegate).onMessage(message);
				return;
			}
		}

		// Regular case: find a handler method reflectively.
		Object convertedMessage = extractMessage(message);

		String methodName = getListenerMethodName(message, convertedMessage);

		if (methodName == null) {
			throw new AmqpIllegalStateException("No default listener method specified: "
					+ "Either specify a non-null value for the 'defaultListenerMethod' property or "
					+ "override the 'getListenerMethodName' method.");
		}

		// Invoke the handler method with appropriate arguments.
		Object[] listenerArguments = buildListenerArguments(convertedMessage);
		Object result = invokeListenerMethod(methodName, listenerArguments, message);
		if (result != null) {
			handleResult(result, message, channel);
		} else {
			logger.trace("No result object given - no result to handle");
		}
	}

	public MyMessageListenerAdapter(Object delegate) {
		super.setDelegate(delegate);
	}

}
