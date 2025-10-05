package com.example.payments.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SPAController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        return "forward:/";
    }
}