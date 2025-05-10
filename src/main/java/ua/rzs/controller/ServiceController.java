package ua.rzs.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import ua.rzs.model.ServiceItem;
import ua.rzs.service.ServiceItemService;

@PreAuthorize("hasAuthority('ADMIN')")
@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceItemService serviceItemService;

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("serviceItem", new ServiceItem());
        return "service-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        ServiceItem serviceItem = serviceItemService.findById(id);
        model.addAttribute("serviceItem", serviceItem);
        return "service-form";
    }

    @PostMapping("/save")
    public String saveService(@ModelAttribute ServiceItem serviceItem) {
        serviceItemService.save(serviceItem);
        return "redirect:/home";
    }

    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        serviceItemService.deleteById(id);
        return "redirect:/home";
    }
}

