package com.doc.appointment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String booking() {
        return "booking";  // Refers to src/main/resources/templates/index.html
    }

    @GetMapping("/view")
    public String view() {
        return "view";
    }

    @GetMapping("/search")
    public String search() {
        return "search";
    }
}
