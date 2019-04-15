package com.cyberway.spring_boot_starter_cqrs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * use spring.rabbitmq instead
 * @author xnq 
 */
@Deprecated
@ConfigurationProperties(prefix = "cqrs")
public class CqrsProperties {

	private String mqHost;
	private String mqPort;

	public String getMqHost() {
		return mqHost;
	}

	public void setMqHost(String mqHost) {
		this.mqHost = mqHost;
	}

	public String getMqPort() {
		return mqPort;
	}

	public void setMqPort(String mqPort) {
		this.mqPort = mqPort;
	}

	public String getMqPasswrod() {
		return mqPasswrod;
	}

	public void setMqPasswrod(String mqPasswrod) {
		this.mqPasswrod = mqPasswrod;
	}

	public String getMqUsername() {
		return mqUsername;
	}

	public void setMqUsername(String mqUsername) {
		this.mqUsername = mqUsername;
	}

	private String mqPasswrod;

	private String mqUsername;
}
