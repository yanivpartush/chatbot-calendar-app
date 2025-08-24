package com.chatbotcal.controller;

import com.chatbotcal.controller.dto.UserDtoResponse;
import com.chatbotcal.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")

public class UserController {

    private UserService userService;
    private Counter allUsersRequestsCounter;
    private Counter userByIdRequestsCounter;

    public UserController(UserService userService, MeterRegistry meterRegistry) {
        this.userService = userService;
        this.allUsersRequestsCounter = Counter.builder("users_all_requests_total")
                .description("No. of requests to getAllUsers")
                .register(meterRegistry);

        this.userByIdRequestsCounter = Counter.builder("users_by_id_requests_total")
                .description("No. of requests to getUserById")
                .register(meterRegistry);
    }

    @GetMapping
    public List<UserDtoResponse> getAllUsers() {
        allUsersRequestsCounter.increment();
        return userService.getAllUsers().stream()
                .map(UserDtoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDtoResponse> getUserById(@PathVariable String id) {
        userByIdRequestsCounter.increment();
        return userService.getUserById(id)
                .map(UserDtoResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
