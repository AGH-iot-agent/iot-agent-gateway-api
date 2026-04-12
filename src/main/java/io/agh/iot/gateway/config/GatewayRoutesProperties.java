package io.agh.iot.gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayRoutesProperties {
    private List<RouteDefinition> routes = new ArrayList<>();

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteDefinition> routes) {
        this.routes = routes;
    }

    public static class RouteDefinition {
        private String path;
        private String service;
        private int port = 8080;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}