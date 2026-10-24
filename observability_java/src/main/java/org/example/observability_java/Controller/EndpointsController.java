package org.example.observability_java.Controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class EndpointsController {

    private final RequestMappingHandlerMapping handlerMapping;

    public EndpointsController(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping("/endpoints")
    public List<String> GetEndpoints() {
        return this.handlerMapping.getHandlerMethods()
                .keySet()
                .stream()
                .filter(req -> !req.getMethodsCondition().getMethods().isEmpty())
                .map(req -> req.getMethodsCondition().getMethods().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")) +  " " + String.join(", ", req.getPatternValues()))
                .collect(Collectors.toList());
    }

}
