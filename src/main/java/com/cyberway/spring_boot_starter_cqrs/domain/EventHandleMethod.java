package com.cyberway.spring_boot_starter_cqrs.domain;

public class EventHandleMethod {

	private String method;

	private String queue;

	public EventHandleMethod(String method, String queue) {
		super();
		this.method = method;
		this.queue = queue;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

}
