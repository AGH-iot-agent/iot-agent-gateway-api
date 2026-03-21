package io.agh.iot.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RouteController {

    private static final Map<String, String> ROUTES = Map.of(
        "/devices", "iot-agent-device-api",
        "/alerts", "iot-agent-alert-api",
        "/logs", "iot-agent-logs",
        "/summary", "iot-agent-dashboard-api"
    );

    @GetMapping("/route")
    public ResponseEntity<RouteResponse> getRoute(@RequestParam String path) {
        String service = ROUTES.get(path);
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new RouteResponse(path, service));
    }

    record RouteResponse(String path, String service) {}
}
