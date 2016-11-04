package com.krrrr38.logback.notify;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class LineNotifySender extends Thread {
    private static final int CONNECT_TIMEOUT = 2000;
    private static final int READ_TIMEOUT = 3000;
    private static final long RETRY_WAIT_MILLIS = 2000L;
    private static final String AGGREGATE_MESSAGE_DELIMITER = "\n";
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final URL url;
    private final String accessToken;
    private final LinkedBlockingQueue<String> queue;
    private final long aggregateMessageWaitMillis;

    LineNotifySender(URL url, String accessToken, Integer queueSize, long aggregateMessageWaitMillis) {
        this.url = url;
        this.accessToken = accessToken;
        this.queue = queueSize == null
                     ? new LinkedBlockingQueue<String>()
                     : new DiscardableQueue<String>(queueSize);
        this.aggregateMessageWaitMillis = aggregateMessageWaitMillis;
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            try {
                Thread.sleep(aggregateMessageWaitMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (queue.isEmpty()) {
                continue;
            }

            send(aggregate(queue));
        }

        while (!queue.isEmpty()) {
            send(aggregate(queue));
        }
    }

    @Override
    public synchronized void start() {
        isRunning.set(true);
        setDaemon(true);
        super.start();
    }

    void shutdown() {
        isRunning.set(false);
    }

    private String aggregate(Queue<String> queue) {
        final StringBuilder sb = new StringBuilder("\n");
        String message;
        boolean isFirst = true;
        while ((message = queue.poll()) != null) {
            if (!isFirst) {
                sb.append(AGGREGATE_MESSAGE_DELIMITER);
            }
            sb.append(message);
            isFirst = false;
        }
        return sb.toString();
    }

    private void send(String message) {
        send(message, false);
    }

    private void send(String message, boolean isRetry) {
        try {
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");

            final String body = "message=" + URLEncoder.encode(message, "UTF-8");

            conn.connect();
            OutputStream out = conn.getOutputStream();
            out.write(body.getBytes("UTF-8"));
            out.flush();
            out.close();
            if (conn.getResponseCode() / 100 != 2 && !isRetry) {
                try {
                    Thread.sleep(RETRY_WAIT_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                send(message, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void append(String message) {
        queue.add(message);
    }
}
