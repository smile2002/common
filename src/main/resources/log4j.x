<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
<log4j:configuration>
    <appender name="STDOUT" class="org.apache.log4j.ConsoleAppender">
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="[%d{yyyy-MM-dd HH:mm:ss.SSS}][%t][%p][%X{LOGID}]%m%n" />
        </layout>
    </appender>
    <!--logger name="sys.log">
        <level value="DEBUG" />
        <appender-ref ref="STDOUT" />
    </logger>
    <logger name="up.at">
        <level value="DEBUG" />
        <appender-ref ref="STDOUT" />
    </logger>
    <logger name="io.netty">
        <level value="ERROR" />
        <appender-ref ref="STDOUT" />
    </logger>
    <logger name="org.apache">
        <level value="ERROR" />
        <appender-ref ref="STDOUT" />
    </logger-->
</log4j:configuration>