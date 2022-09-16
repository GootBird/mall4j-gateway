package com.xixi.mall.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Primary
public class SwaggerResourceConfig implements SwaggerResourcesProvider {

    private static final Logger log = LoggerFactory.getLogger(SwaggerResourceConfig.class);

    private final RouteLocator routeLocator;

    private final GatewayProperties gatewayProperties;

    public SwaggerResourceConfig(RouteLocator routeLocator, GatewayProperties gatewayProperties) {
        this.routeLocator = routeLocator;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public List<SwaggerResource> get() {

        List<String> routes = new ArrayList<>();
        routeLocator.getRoutes().subscribe(route -> routes.add(route.getId()));

        return gatewayProperties.getRoutes()
                .stream()
                .filter(routeDefinition -> routes.contains(routeDefinition.getId()))
                .flatMap(routeDefinition -> routeDefinition.getPredicates()
                        .stream()
                        .filter(predicateDefinition -> ("Path").equalsIgnoreCase(predicateDefinition.getName()))
                        .map(predicateDefinition ->
                                swaggerResource(
                                        routeDefinition.getId(),
                                        predicateDefinition.getArgs()
                                                .get(NameUtils.GENERATED_NAME_PREFIX + "0")
                                                .replace("**", "v2/api-docs")
                                )
                        )
                )
                .collect(Collectors.toList());
    }

    private SwaggerResource swaggerResource(String name, String location) {
        log.info("name:{},location:{}", name, location);
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }

}
