package com.zz.fintrack;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/")
    public String home() {
        return "Financial-API is running ✅";
    }
}
