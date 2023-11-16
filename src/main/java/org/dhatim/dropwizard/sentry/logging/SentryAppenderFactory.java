package org.dhatim.dropwizard.sentry.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.common.AbstractAppenderFactory;
import io.dropwizard.logging.common.async.AsyncAppenderFactory;
import io.dropwizard.logging.common.filter.LevelFilterFactory;
import io.dropwizard.logging.common.layout.LayoutFactory;
import io.sentry.Sentry;
import io.sentry.SentryOptions;
import io.sentry.logback.SentryAppender;
import jakarta.validation.constraints.NotNull;
import org.dhatim.dropwizard.sentry.SentryConfigurator;
import org.dhatim.dropwizard.sentry.filters.DroppingSentryLoggingFilter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonTypeName("sentry")
public class SentryAppenderFactory extends AbstractAppenderFactory<ILoggingEvent> {

    private static final String APPENDER_NAME = "dropwizard-sentry";

    @NotNull
    @JsonProperty
    public String dsn = null;

    @JsonProperty
    public String environment = null;

    @JsonProperty
    public Map<String, String> tags = null;

    @JsonProperty
    public String release = null;

    @JsonProperty
    public String serverName = null;

    @JsonProperty
    public List<String> inAppIncludes = null;

    @JsonProperty
    public List<String> inAppExcludes = null;

    @JsonProperty
    public String configurator = null;

    @JsonProperty
    public List<String> contextTags = null;

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context,
                                         String applicationName,
                                         LayoutFactory<ILoggingEvent> layoutFactory,
                                         LevelFilterFactory<ILoggingEvent> levelFilterFactory,
                                         AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {
        checkNotNull(context);
        SentryOptions options = new SentryOptions();
        options.setDsn(dsn);
        Optional.ofNullable(environment).ifPresent(options::setEnvironment);
        Optional.ofNullable(tags).ifPresent(tags -> tags.forEach(options::setTag));
        Optional.ofNullable(release).ifPresent(options::setRelease);
        Optional.ofNullable(serverName).ifPresent(options::setServerName);
        Optional.ofNullable(inAppIncludes).ifPresent(inAppIncludes -> inAppIncludes.forEach(options::addInAppInclude));
        Optional.ofNullable(inAppExcludes).ifPresent(inAppExcludes -> inAppExcludes.forEach(options::addInAppExclude));
        Optional.ofNullable(contextTags).ifPresent(contextTags -> contextTags.forEach(options::addContextTag));
        Optional.ofNullable(configurator).ifPresent(configurator -> {
            try {
                Class<?> klass = Class.forName(configurator);
                if (!SentryConfigurator.class.isAssignableFrom(klass)) {
                    throw new IllegalArgumentException("configurator class " + configurator + " does not implement " + SentryConfigurator.class.getName());
                }
                SentryConfigurator sentryConfigurator = ((Class<SentryConfigurator>) klass).getDeclaredConstructor().newInstance();
                sentryConfigurator.configure(options);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("configurator class " + configurator + " not found", e);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("configurator class " + configurator + " does not define a default constructor", e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException("cannot invoke default constructor on configurator class " + configurator, e);
            }
        });

        // close Sentry if previously initialized for bootstrap
        Sentry.close();
        SentryAppender appender = new SentryAppender();

        appender.setOptions(options);
        appender.setName(APPENDER_NAME);
        appender.setContext(context);
        appender.setMinimumBreadcrumbLevel(threshold);
        appender.setMinimumEventLevel(threshold);

        appender.addFilter(levelFilterFactory.build(threshold));
        getFilterFactories().forEach(f -> appender.addFilter(f.build()));
        appender.start();

        final Filter<ILoggingEvent> filter = new DroppingSentryLoggingFilter();
        filter.start();
        appender.addFilter(filter);

        return appender;
    }

}
