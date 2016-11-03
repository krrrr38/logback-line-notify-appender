package com.krrrr38.logback.notify;

import java.util.concurrent.LinkedBlockingQueue;

class DiscardableQueue<E> extends LinkedBlockingQueue<E> {
    private final int queueSize;

    DiscardableQueue(int queueSize) {
        this.queueSize = queueSize;
    }

    @Override
    public boolean offer(E e) {
        if (size() >= queueSize) {
            remove();
        }
        return super.offer(e);
    }
}
