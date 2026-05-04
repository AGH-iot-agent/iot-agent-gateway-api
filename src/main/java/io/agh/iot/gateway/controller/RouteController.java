package io.agh.iot.gateway.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.agh.iot.gateway.config.GatewayRoutesProperties;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class RouteController {

    private static final Logger log = LoggerFactory.getLogger(RouteController.class);
    private final Map<String, RouteTarget> routes;
    private final RestTemplate restTemplate = new RestTemplate();

    public RouteController(GatewayRoutesProperties gatewayRoutesProperties) {
        this.routes = gatewayRoutesProperties.getRoutes()
            .stream()
            .collect(LinkedHashMap::new,
                (accumulator, route) -> {
                    log.info("Mapping route: {} -> {}:{}", route.getPath(), route.getService(), route.getPort());
                    accumulator.put(route.getPath(), new RouteTarget(route.getService(), route.getPort()));
                },
                LinkedHashMap::putAll);
        log.info("[RouteController] Mapowanie tras zakończone. Zmapowane: {}", this.routes);
    }

    @GetMapping("/routes")
    public List<RouteResponse> getRoutes() {
        log.info("GET /routes called");
        return routes.entrySet()
            .stream()
            .map(entry -> new RouteResponse(entry.getKey(), entry.getValue().service(), entry.getValue().port()))
            .toList();
    }

    @GetMapping("/api/route")
    public ResponseEntity<RouteResponse> getRoute(@RequestParam String path) {
        log.info("GET /route called with path={}", path);
        RouteTarget target = routes.get(path);
        if (target == null) {
            log.warn("Route not found for path={}", path);
            return ResponseEntity.notFound().build();
        }
        log.info("Route found for path={}, service={}:{}", path, target.service(), target.port());
        return ResponseEntity.ok(new RouteResponse(path, target.service(), target.port()));
    }

    @RequestMapping(value = "/api/**", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> proxy(HttpServletRequest request, @RequestHeader HttpHeaders headers, @RequestBody(required = false) String body) {
        String requestUri = request.getRequestURI();
        RouteTarget target = routes.get(requestUri);
        if (target == null) {
            String path = requestUri.startsWith("/api") ? requestUri.substring("/api".length()) : requestUri;
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            String[] segments = path.split("/");
            if (segments.length < 2 || segments[1].isEmpty()) {
                log.warn("Invalid API path: {}", path);
                return ResponseEntity.notFound().build();
            }
            String serviceKey = "/" + segments[1];
            target = routes.get(serviceKey);
            if (target == null) {
                log.warn("Route not found for path={} or serviceKey={}", requestUri, serviceKey);
                return ResponseEntity.notFound().build();
            }
        }
        String url = "http://" + target.service() + ":" + target.port() + requestUri;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        log.info("Proxying {} to {}", requestUri, url);
        ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(response.getHeaders());
        responseHeaders.remove("Transfer-Encoding");
        return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
    }

    public record RouteResponse(String path, String service, int port) {}

    private record RouteTarget(String service, int port) {}

}
