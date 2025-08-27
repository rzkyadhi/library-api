package com.itsec.technical_test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Hello")
public class HelloWorldController {

    @GetMapping("/")
    @Operation(summary = "Return a simple greeting message")
    public String sayHello() {
        return "Hello World";
    }
}
