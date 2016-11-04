# logback-line-notify-appender

Logback Appender for [LINE Notify](https://notify-bot.line.me).

## Setup

- [Issue Access Token](https://notify-bot.line.me/my/)

- pom.xml

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.krrrr38/logback-line-notify-appender/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.krrrr38%22%20logback-line-notify-appender)

```xml
<dependency>
  <groupId>com.krrrr38</groupId>
  <artifactId>logback-line-notify-appender</artifactId>
  <version>${version}</version>
</dependency>
```

- logback.xml

```xml
<appender name="LINE_NOTIFY" class="com.krrrr38.logback.notify.LineNotifyAppender">
  <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
    <level>WARN</level>
  </filter>
  <layout>
    <pattern>%d{HH:mm:ss.SSS} %-5level %logger{0}%n%msg%n%ex{2}</pattern>
  </layout>
  <accessToken>YOUR_LINE_NOTIFY_ACCESSS_TOKEN</accessToken> // mandatory
  <endpoint>https://notify-api.line.me/api/notify</endpoint> // optional
  <queueSize>10</queueSize> // optional, if not set use LinkedBlockingQueue directly.
</appender>

<root level="INFO">
  <appender-ref ref="LINE_NOTIFY"/>
</root>
```

## Dev Tools

### Release

```sh
make release
```

### SNAPSHOT Release

```sh
make snapshot
```
