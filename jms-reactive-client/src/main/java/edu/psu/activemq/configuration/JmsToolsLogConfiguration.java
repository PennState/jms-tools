package edu.psu.activemq.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.spi.ContextAwareBase;
import edu.psu.activemq.util.PropertyUtil;

/*
 * Copyright (c) 2018 by The Pennsylvania State University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class JmsToolsLogConfiguration extends ContextAwareBase implements Configurator {

    public JmsToolsLogConfiguration() {

    }

    @Override
    public void configure(LoggerContext lc) {

        PatternLayoutEncoder ple = new PatternLayoutEncoder();

        ple.setPattern("%d{HH:mm:ss.SSS} [%X{uniqueId}] [%X{correlationId}] [%thread] %-5level %logger - %msg%xException%n");
        ple.setContext(lc);
        ple.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
        consoleAppender.setEncoder(ple);
        consoleAppender.setContext(lc);
        consoleAppender.start();

        String config_console_root_log_level = PropertyUtil.getProperty("CONSOLE_ROOT_LOG_LEVEL");
        Level rootLevel = Level.toLevel(config_console_root_log_level, Level.WARN);

        String config_console_log_level = PropertyUtil.getProperty("CONSOLE_LOG_LEVEL");
        Level psuPackageLevel = Level.toLevel(config_console_log_level, Level.INFO);

        Logger psuPackageLogger = lc.getLogger("edu.psu");
        psuPackageLogger.addAppender(consoleAppender);
        psuPackageLogger.setLevel(psuPackageLevel);

        Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(consoleAppender);
        rootLogger.setLevel(rootLevel);

        addInfo("Logback configuration based on root " + rootLevel + " and edu.psu " + psuPackageLevel);
        System.out.println("Logback configuration based on root " + rootLevel + " and edu.psu " + psuPackageLevel);
    }

}
