package ua.rzs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ua.rzs.service.ServiceItemService;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ServiceItemService serviceService;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("services", serviceService.findAll());
        return "home";
    }
}
