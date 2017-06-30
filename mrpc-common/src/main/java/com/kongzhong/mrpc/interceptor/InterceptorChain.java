package com.kongzhong.mrpc.interceptor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 拦截器链
 */
public class InterceptorChain {

    private List<Entry> interceptors = Lists.newArrayList();
    private Set<String> registeredNames = Sets.newHashSet();
    private Lock lock = new ReentrantLock();

    public InterceptorChain() {

    }

    public InterceptorChain addLast(String name, RpcInterceptor interceptor) {
        lock.lock();
        try {
            checkDuplicateName(name);
            Entry entry = new Entry(name, interceptor);
            register(interceptors.size(), entry);
            return this;
        } finally {
            lock.unlock();
        }
    }

    public InterceptorChain addFirst(String name, RpcInterceptor interceptor) {
        lock.lock();
        try {
            checkDuplicateName(name);
            Entry entry = new Entry(name, interceptor);
            register(0, entry);
            return this;
        } finally {
            lock.unlock();
        }
    }

    public InterceptorChain addBefore(String baseName, String name, RpcInterceptor interceptor) {
        lock.lock();
        try {
            checkDuplicateName(name);
            int index = getInterceptorIndex(baseName);
            if (index == -1)
                throw new NoSuchElementException(baseName);
            Entry entry = new Entry(name, interceptor);
            register(index, entry);
            return this;
        } finally {
            lock.unlock();
        }
    }

    public InterceptorChain addAfter(String baseName, String name, RpcInterceptor interceptor) {
        lock.lock();
        try {
            checkDuplicateName(name);
            int index = getInterceptorIndex(baseName);
            if (index == -1)
                throw new NoSuchElementException(baseName);
            Entry entry = new Entry(name, interceptor);
            register(index + 1, entry);
            return this;
        } finally {
            lock.unlock();
        }

    }

    private int getInterceptorIndex(String name) {
        List<Entry> interceptors = this.interceptors;
        for (int i = 0; i < interceptors.size(); i++) {
            if (interceptors.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private void register(int index, Entry entry) {
        interceptors.add(index, entry);
        registeredNames.add(entry.name);
    }

    private void checkDuplicateName(String name) {
        if (registeredNames.contains(name)) {
            throw new IllegalArgumentException("Duplicate interceptor name: " + name);
        }
    }

    public List<RpcInterceptor> getInterceptors() {
        if (null != interceptors && !interceptors.isEmpty()) {
            List<RpcInterceptor> list = new ArrayList<>(this.interceptors.size());
            for (Entry entry : this.interceptors) {
                list.add(entry.interceptor);
            }
            return Collections.unmodifiableList(list);
        } else {
            return new ArrayList<>(0);
        }
    }

    static class Entry {
        private String name;
        private RpcInterceptor interceptor;

        public Entry(String name, RpcInterceptor interceptor) {
            this.name = name;
            this.interceptor = interceptor;
        }
    }

}