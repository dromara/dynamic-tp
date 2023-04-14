package com.dtp.core.support.callable;

import com.dtp.core.support.Ordered;

import java.util.concurrent.Callable;

/**
 * OrderedRunnable related
 *
 * @param <C> the result type of method
 * @author yanhom
 * @since 1.0.0
 **/
public interface OrderedCallable<C> extends Ordered, Callable<C> {

}
