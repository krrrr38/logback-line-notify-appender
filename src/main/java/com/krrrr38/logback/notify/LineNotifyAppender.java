package com.krrrr38.logback.notify;

import java.net.MalformedURLException;
import java.net.URL;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class LineNotifyAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private LineNotifySender lineNotifySender;
    private String endpoint = "https://notify-api.line.me/api/notify";
    private long aggregateMessageWaitMillis = 2000L;
    private Layout<ILoggingEvent> layout;
    private Integer queueSize;
    private String accessToken;

    protected void append(ILoggingEvent iLoggingEvent) {
        final String message = layout == null
                               ? iLoggingEvent.getFormattedMessage()
                               : layout.doLayout(iLoggingEvent);
        lineNotifySender.append(message);
    }

    @Override
    public void start() {
        if (accessToken == null) {
            throw new IllegalStateException("LineNotifyAppender requires accessToken parameter");
        }

        if (lineNotifySender == null) {
            try {
                final URL url = new URL(endpoint);
                lineNotifySender = new LineNotifySender(
                        url,
                        accessToken,
                        queueSize,
                        aggregateMessageWaitMillis
                );
                lineNotifySender.start();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        super.start();
    }

    @Override
    public void stop() {
        if (lineNotifySender != null) {
            lineNotifySender.shutdown();
            try {
                lineNotifySender.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        super.stop();
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setAggregateMessageWaitMillis(long aggregateMessageWaitMillis) {
        this.aggregateMessageWaitMillis = aggregateMessageWaitMillis;
    }
}
