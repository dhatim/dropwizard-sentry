package org.dhatim.dropwizard.sentry.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.logging.common.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.common.filter.ThresholdLevelFilterFactory;
import io.dropwizard.logging.common.layout.DropwizardLayoutFactory;
import io.sentry.SentryOptions;
import io.sentry.logback.SentryAppender;
import org.dhatim.dropwizard.sentry.SentryConfigurator;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void buildSentryAppenderFullConfiguration() {
        SentryAppenderFactory factory = new SentryAppenderFactory();
        factory.dsn = "https://user:pass@app.sentry.io/id";
        factory.configurator = "org.dhatim.dropwizard.sentry.logging.SentryAppenderFactoryTest$CaptureSentryConfigurator";
        factory.contextTags = List.of("contextTag1");
        factory.release = "1.0.0";
        factory.environment = "test";
        factory.serverName = "10.0.0.1";

        Appender<ILoggingEvent> appender = factory.build(context, "", layoutFactory, levelFilterFactory, asyncAppenderFactory);
        assertThat(appender, instanceOf(SentryAppender.class));

        SentryOptions capturedOptions = CaptureSentryConfigurator.capturedOptions;
        assertNotNull(capturedOptions);

        assertEquals("https://user:pass@app.sentry.io/id", capturedOptions.getDsn());
        assertEquals("test", capturedOptions.getEnvironment());
        assertEquals("1.0.0", capturedOptions.getRelease());
        assertThat(capturedOptions.getContextTags(), IsIterableContainingInOrder.contains("contextTag1"));
        assertEquals("10.0.0.1", capturedOptions.getServerName());
    }

    protected static class CaptureSentryConfigurator implements SentryConfigurator {
        static SentryOptions capturedOptions;
        @Override
        public void configure(SentryOptions options) {
            capturedOptions = options;
        }
    }

}
