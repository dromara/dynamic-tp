package org.dromara.dynamictp.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * ClassName: VTTaskStats
 * Package: org.dromara.dynamictp.common.entity
 * Description:
 *  Wanted performance is like:
 *   "virtual_threads": [
 *     {
 *       "id": 1,
 *       "name": "VirtualThread-1",
 *       "state": "RUNNABLE",
 *       "stack_trace": [
 *         {
 *           "class": "java.base/java.lang.Thread",
 *           "method": "lambda$main$0",
 *           "file": "Main.java",
 *           "line": 10
 *         }
 *       ]
 *     },
 *     {
 *       "id": 2,
 *       "name": "VirtualThread-2",
 *       "state": "BLOCKED",
 *       "stack_trace": [
 *         {
 *           "class": "java.base/java.net.SocketInputStream",
 *           "method": "socketRead0",
 *           "file": "SocketInputStream.java",
 *           "line": 61
 *         }
 *       ]
 *     }
 * @author CYC
 * @create 2024/11/4 16:52
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VTTaskStats extends Metrics{

    /**
     * 虚拟线程的id
     */
    private int id;

    /**
     * 虚拟线程的名称
     */
    private String name;

    /**
     *  虚拟线程的状态
     */
    private String state;

    /**
     *  虚拟线程所承载任务的所有堆栈信息
     */
    private List<String> stack;

    /**
     * 虚拟线程所承载任务的理想的堆栈信息
     */
    private StackForGood stackForGood;

    /**
     * 虚拟线程的任务数
     */
    private int taskCount;

    private class StackForGood {
        /**
         * Wanted stack trace is like:
         *  "stack_trace": [
         *         {
         *           "class": "java.base/java.net.SocketInputStream",
         *           "method": "socketRead0",
         *           "file": "SocketInputStream.java",
         *           "line": 61
         *         }
         *       ]
         */
        private String clazz;

        private String method;

        private String file;

        private String line;
    }
}
