package dev.sitconsulting.library.spring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Auto-configuration for application metrics.
 * Provides build information metrics that can be customized via properties:
 * <p>
 * Application configuration (in application.properties/yaml):
 * <pre>
 * app.build.version=${project.version}
 * app.build.timestamp=${maven.build.timestamp:0}
 * </pre>
 */
@Configuration
@Slf4j
public class MetricsAutoConfiguration {

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> meterRegistryDefaultsCustomizer(
            Environment environment) {
        BuildInfo bootInfo = getBuildInfoFromOwnPackage();

        log.info("Configuring meter registry with app name: {}, build version: {}", appName, bootInfo.version);

        return registry -> {
            // Add common tags
            registry.config().commonTags(
                    "application", appName,
                    "host", getHost()
            );

            // Register build_info gauge with tags for filtering
            Gauge.builder("buildInfo", bootInfo, v -> 1.0)
                    .description("Build information")
                    .tag("version", bootInfo.version)
                    .tag("buildTime", bootInfo.time)
                    .register(registry);

            // Configure percentiles for all metrics
            registry.config().meterFilter(
                    new MeterFilter() {
                        @Override
                        public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                            return DistributionStatisticConfig.builder()
                                    .percentiles(0.99d, 0.95d, 0.90d, 0.75d)
                                    .build()
                                    .merge(config);
                        }
                    }
            );
        };
    }

    private static String getHost() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            log.warn("Could not resolve host name, using fallback...");
            return "<unknown>";
        }
    }

    private static BuildInfo getBuildInfoFromOwnPackage() {
        try (InputStream is = MetricsAutoConfiguration.class.getClassLoader()
                .getResourceAsStream("META-INF/build-info.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String version = props.getProperty("build.version");
                String time = props.getProperty("build.time");
                if (version != null) {
                    return new BuildInfo(version, time);
                }
            }
        } catch (IOException | NumberFormatException e) {
            log.debug("Could not load build-info.properties", e);
        }
        return new BuildInfo("unknown", LocalDateTime.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    private static class BuildInfo {
        final String version;
        final String time;

        BuildInfo(String version, String time) {
            this.version = version;
            this.time = time;
        }
    }
}
