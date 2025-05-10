package ua.rzs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.rzs.model.Order;
import ua.rzs.model.OrderFile;
import ua.rzs.service.OrderService;

import java.io.InputStream;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/order/{serviceItemId}")
    public String showOrderForm(@PathVariable Long serviceItemId, Model model) {
        model.addAttribute("serviceItemId", serviceItemId);
        return "order-form";
    }

    @PostMapping("/order")
    public String createOrder(@RequestParam("files") MultipartFile[] files,
                              @RequestParam("comment") String comment,
                              @RequestParam("serviceItemId") Long serviceItemId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.placeOrder(files, comment, principal.getName(), serviceItemId);
            redirectAttributes.addFlashAttribute("message", "Замовлення успішно оформлено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка при оформленні замовлення: " + e.getMessage());
        }
        return "redirect:/home";
    }

    @PostMapping("/orders/{id}/status")
    @Secured({"ADMIN"})
    public String changeOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus newStatus,
            @RequestParam Long userId,
            RedirectAttributes ra
    ) {
        orderService.updateStatus(id, newStatus);
        ra.addFlashAttribute("message","Статус оновлено");
        return "redirect:/profile/" + userId;
    }

    @GetMapping("order/download/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN') or @orderFileRepo.findById(#id).get().order.user.email == principal.username")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        return orderService.findFileById(id);
    }
}

