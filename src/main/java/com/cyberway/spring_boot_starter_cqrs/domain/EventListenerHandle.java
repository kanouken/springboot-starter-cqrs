package com.cyberway.spring_boot_starter_cqrs.domain;

import java.util.List;

public class EventListenerHandle {

	private Class<? extends Object> clazz;

	public Class<? extends Object> getClazz() {
		return clazz;
	}

	public void setClazz(Class<? extends Object> clazz) {
		this.clazz = clazz;
	}

	List<EventHandleMethod> methods;

	public List<EventHandleMethod> getMethods() {
		return methods;
	}

	public void setMethods(List<EventHandleMethod> methods) {
		this.methods = methods;
	}

}
