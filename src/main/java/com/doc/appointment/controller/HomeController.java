package com.doc.appointment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";  // Refers to src/main/resources/templates/index.html
    }

    @GetMapping("/dash")
    public String dashboard() {
        return "dashboard";
    }
}
