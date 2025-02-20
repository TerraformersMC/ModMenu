package com.terraformersmc.modmenu.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateCheckerThreadFactory implements ThreadFactory {
	static final AtomicInteger COUNT = new AtomicInteger(-1);

	@Override
	public Thread newThread(@NotNull Runnable r) {
		return Thread.ofVirtual().name("ModMenu/Update Checker/%s".formatted(COUNT.incrementAndGet())).unstarted(r);
	}
}
