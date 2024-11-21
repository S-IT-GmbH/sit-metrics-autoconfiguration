package dev.sitconsulting.library.spring;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Slf4j
public class MetricsAutoConfiguration {
    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> meterRegistryDefaultsCustomizer() {
        return registry -> registry.config()
                .commonTags(
                        "application", appName,
                        "host", getHost()
                ).meterFilter(
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
}