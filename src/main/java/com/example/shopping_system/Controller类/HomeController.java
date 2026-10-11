package com.example.shopping_system.Controller类;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/seller.html")
    public String seller() {
        return "seller";
    }
}
