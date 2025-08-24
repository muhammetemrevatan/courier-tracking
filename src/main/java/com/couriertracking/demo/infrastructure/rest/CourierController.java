package com.couriertracking.demo.infrastructure.rest;

import com.couriertracking.demo.application.event.LocationEventPublisher;
import com.couriertracking.demo.application.port.in.CourierCommandPort;
import com.couriertracking.demo.application.port.in.CourierQueryPort;
import com.couriertracking.demo.domain.model.Location;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RestController
@RequestMapping("/api/couriers")
@Tag(name = "Couriers")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class CourierController {

    CourierCommandPort commandPort;
    CourierQueryPort queryPort;
    LocationEventPublisher publisher;

    public record CreateCourierRequest(String name) {}
    public record CreateCourierResponse(Long id, String name) {}
    public record CourierLocationRequest(double lat, double lng, LocalDateTime time) {}
    public record CourierDistanceResponse(Long courierId, double totalDistance) {}
    public record CourierLogResponse(Long courierId, Long storeId, LocalDateTime entryTime) {}

    @PostMapping
    public ResponseEntity<CreateCourierResponse> create(@RequestBody CreateCourierRequest req) {
        Long id = commandPort.createCourier(req.name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateCourierResponse(id, req.name()));
    }

    @PostMapping("/{courierId}/location")
    @Operation(summary = "Send courier location")
    public ResponseEntity<Void> sendLocation(@PathVariable Long courierId,
                                             @RequestBody CourierLocationRequest req) {
        publisher.notifyObservers(courierId, new Location(req.lat(), req.lng(), req.time()));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{courierId}/distance")
    @Operation(summary = "Get courier total distance")
    public CourierDistanceResponse getDistance(@PathVariable Long courierId) {
        return new CourierDistanceResponse(courierId, queryPort.getTotalTravelDistance(courierId));
    }

    @GetMapping("/{courierId}/logs")
    @Operation(summary = "Get courier logs")
    public List<CourierLogResponse> getLogs(@PathVariable Long courierId) {
        return queryPort.getLogs(courierId).stream()
                .map(courierLog -> new CourierLogResponse(courierLog.courierId(), courierLog.storeId(), courierLog.entryTime()))
                .toList();
    }
}