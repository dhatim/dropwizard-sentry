package org.dhatim.dropwizard.sentry.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.logging.common.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.common.filter.ThresholdLevelFilterFactory;
import io.dropwizard.logging.common.layout.DropwizardLayoutFactory;
import io.sentry.logback.SentryAppender;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SentryAppenderFactoryTest {

    private final LoggerContext context = new LoggerContext();
    private final DropwizardLayoutFactory layoutFactory = new DropwizardLayoutFactory();
    private final ThresholdLevelFilterFactory levelFilterFactory = new ThresholdLevelFilterFactory();
    private final AsyncLoggingEventAppenderFactory asyncAppenderFactory = new AsyncLoggingEventAppenderFactory();

    @Test
    public void hasValidDefaults() throws IOException, ConfigurationException {
        final SentryAppenderFactory factory = new SentryAppenderFactory();

        assertNull(factory.dsn, "default dsn is unset");
        assertNull(factory.environment, "default environment is unset");
        assertNull(factory.release, "default release is unset");
        assertNull(factory.serverName, "default serverName is unset");
    }

    @Test
    public void buildSentryAppenderShouldFailWithNullContext() {
        assertThrows(NullPointerException.class,
                () -> new SentryAppenderFactory().build(null, "", null, levelFilterFactory, asyncAppenderFactory));
    }

    @Test
    public void buildSentryAppenderShouldWorkWithValidConfiguration() {
        SentryAppenderFactory factory = new SentryAppenderFactory();
        factory.dsn = "https://user:pass@app.sentry.io/id";

        Appender<ILoggingEvent> appender = factory.build(context, "", layoutFactory, levelFilterFactory, asyncAppenderFactory);

        assertThat(appender, instanceOf(SentryAppender.class));
    }

}
