package com.monjam.example.controller;

import com.monjam.example.domain.entity.User;
import com.monjam.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public Mono<User> findById(@PathVariable String id) {
        return userRepository.findById(id);
    }
}
