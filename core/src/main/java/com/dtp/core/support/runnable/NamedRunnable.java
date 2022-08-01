package com.dtp.core.support.runnable;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * NamedRunnable related
 *
 * @author yanhom
 * @since 1.0.6
 */
public class NamedRunnable implements Runnable {

    private final Runnable runnable;

    private final String name;

    public NamedRunnable(Runnable runnable, String name) {
        this.runnable = runnable;
        this.name = name;
    }

    @Override
    public void run() {
        this.runnable.run();
    }

    public String getName() {
        return name;
    }

    public static NamedRunnable of(Runnable runnable, String name) {
        if (StringUtils.isBlank(name)) {
            name = runnable.getClass().getSimpleName() + "-" + UUID.randomUUID();
        }
        return new NamedRunnable(runnable, name);
    }
}
