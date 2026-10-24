package org.example.observability_java.Metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.cumulative.CumulativeCounter;
import io.micrometer.core.instrument.cumulative.CumulativeTimer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class TrackGoldenMetricsAspect {

    private final MeterRegistry registry;
    private final Tags customTags;

    public TrackGoldenMetricsAspect(MeterRegistry registry, Environment env) {
        this.registry = registry;

        this.customTags = Tags.of(
                "pod", env.getProperty("POD_NAME", "unknown"),
                "datacenter", env.getProperty("DATACENTER", "unknown"),
                "deployment", env.getProperty("DEPLOYMENT_NAME", "unknown")
        );
    }

    @Around("@annotation(TrackGoldenMetrics)")
    public Object trackMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        String status = "success";
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            status = "error";
            throw e;
        } finally {

            //TODO добавить теги
            long duration = System.nanoTime() - start;
            String endpoint = getEndpointName(joinPoint);
            Timer.Builder timer = Timer.builder("http_timer_golden_metrics")
                    .tag("status", status)
                    .tag("endpoint", endpoint)
                    .tags(customTags)
                    .publishPercentileHistogram();
            timer.register(registry).record(duration, TimeUnit.NANOSECONDS);

            Counter.Builder counter = Counter.builder("http_counter_golden_metrics")
                    .tag("status", status)
                    .tag("endpoint", endpoint)
                    .tags(customTags);
            counter.register(registry).increment();
        }
    }

    private String getEndpointName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        if (requestMapping != null && requestMapping.value().length > 0) {
            return requestMapping.value()[0]; // Например, "/users/{id}"
        }

        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

}
