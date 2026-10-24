package org.example.observability_java.Metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration(after = MetricsAutoConfiguration.class)
@ConditionalOnClass(MeterRegistry.class)
public class TrackGoldenSignalsAutoConfiguration {

    @Bean
    public TrackGoldenMetricsAspect trackGoldenMetricsAspect(MeterRegistry registry, Environment env) {
        return new TrackGoldenMetricsAspect(registry, env);
    }
}
