package org.dhatim.dropwizard.sentry.logging;

import ch.qos.logback.classic.Logger;
import io.dropwizard.logging.common.filter.ThresholdLevelFilterFactory;
import io.dropwizard.logging.common.layout.DropwizardLayoutFactory;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * A class adding a configured
 * {@link io.sentry.logback.SentryAppender} to the root logger.
 */
public final class SentryBootstrap {

    private SentryBootstrap() {
        /* No instance methods */
    }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn The DSN (Data Source Name) for your project
     */
    public static void bootstrap(final String dsn) {
        bootstrap(dsn, true);
    }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn             The DSN (Data Source Name) for your project
     * @param cleanRootLogger If true, detach and stop all other appenders from
     *                        the root logger
     */
    public static void bootstrap(String dsn, boolean cleanRootLogger) {
        bootstrap(dsn, null, null, cleanRootLogger);
    }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn             The DSN (Data Source Name) for your project
     * @param environment     The environment name to pass to Sentry
     * @param release         The release name to pass to Sentry
     * @param cleanRootLogger If true, detach and stop all other appenders from
     *                        the root logger
     */
    public static void bootstrap(String dsn, String environment, String release, boolean cleanRootLogger) {
        bootstrap(dsn, environment, release, null, cleanRootLogger);
    }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn             The DSN (Data Source Name) for your project
     * @param environment     The environment name to pass to Sentry
     * @param release         The release name to pass to Sentry
     * @param serverName      The server name to pass to Sentry
     * @param cleanRootLogger If true, detach and stop all other appenders from
     *                        the root logger
     */
    public static void bootstrap(String dsn, String environment, String release, String serverName, boolean cleanRootLogger) {
        bootstrap(dsn, null, environment, release, null, cleanRootLogger);
    }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn             The DSN (Data Source Name) for your project
     * @param threshold       log events threshold
     * @param environment     The environment name to pass to Sentry
     * @param release         The release name to pass to Sentry
     * @param serverName      The server name to pass to Sentry
     * @param cleanRootLogger If true, detach and stop all other appenders from
     *                        the root logger
     */
    public static void bootstrap(String dsn, String threshold, String environment, String release, String serverName, boolean cleanRootLogger) {
        SentryAppenderFactory factory = new SentryAppenderFactory();
        factory.dsn = dsn;
        factory.environment = environment;
        factory.release = release;
        factory.serverName = serverName;
        Optional.ofNullable(threshold).ifPresent(factory::setThreshold);
        registerAppender(dsn, cleanRootLogger, factory);
    }

    private static void registerAppender(String dsn, boolean cleanRootLogger, SentryAppenderFactory factory) {
        Logger root = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
        if (cleanRootLogger) {
            root.detachAndStopAllAppenders();
        }
        ThresholdLevelFilterFactory levelFilterFactory = new ThresholdLevelFilterFactory();
        DropwizardLayoutFactory layoutFactory = new DropwizardLayoutFactory();
        root.addAppender(factory.build(root.getLoggerContext(), dsn, layoutFactory, levelFilterFactory, null));
    }

    public static class Builder {

        private final String dsn;
        private String threshold = null;
        private String environment = null;
        private String release = null;
        private String serverName = null;
        private boolean cleanRootLogger;

        public static Builder withDsn(String dsn) {
            Builder builder = new Builder(dsn);
            return builder;
        }

        /**
         * @param dsn The DSN (Data Source Name) for your project
         */
        private Builder(String dsn) {
            this.dsn = dsn;
        }

        /**
         * @param threshold log events threshold
         * @return this builder
         */
        public Builder withThreshold(String threshold) {
            this.threshold = threshold;
            return this;
        }

        /**
         * @param environment The environment name to pass to Sentry
         * @return this builder
         */
        public Builder withEnvironment(String environment) {
            this.environment = environment;
            return this;
        }

        /**
         * @param release The release name to pass to Sentry
         * @return this builder
         */
        public Builder withRelease(String release) {
            this.release = release;
            return this;
        }

        /**
         * @param serverName The server name to pass to Sentry
         * @return this builder
         */
        public Builder withServerName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        /**
         * @param cleanRootLogger If true, detach and stop all other appenders from
         *                        the root logger
         * @return this builder
         */
        public Builder withCleanRootLogger(boolean cleanRootLogger) {
            this.cleanRootLogger = cleanRootLogger;
            return this;
        }

        /**
         * Bootstrap the SLF4J root logger with a configured
         * {@link io.sentry.logback.SentryAppender}.
         */
        public void bootstrap() {
            SentryBootstrap.bootstrap(dsn, threshold, environment, release, serverName, cleanRootLogger);
        }
    }
}
