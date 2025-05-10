package ua.rzs.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.rzs.model.Order;
import ua.rzs.model.User;
import ua.rzs.service.OrderService;
import ua.rzs.service.UserService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final OrderService orderService;

    @Secured({"ADMIN"})
    @GetMapping("/all")
    public String listProfiles(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "profiles";
    }

    @GetMapping("/{id}")
    @Secured({"ADMIN"})
    public String viewProfile(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        List<Order> orders = orderService.findByUserId(user.getId());
        model.addAttribute("orders", orders);
        return "profile";
    }

    @GetMapping
    public String viewMyProfile(Principal principal, Model model) {
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("user", user);
        List<Order> orders = orderService.findByUserId(user.getId());
        model.addAttribute("orders", orders);
        return "profile";
    }

    @PostMapping("/update-email")
    public String updateEmail(@RequestParam String newEmail,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.initiateEmailUpdate(principal.getName(), newEmail);
            redirectAttributes.addFlashAttribute("message", "Код підтвердження надіслано на нову пошту. Будь ласка, введіть його.");
            redirectAttributes.addFlashAttribute("showVerifyEmailForm", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        }
        return "verify-new-email";
    }

    @PostMapping("/verify-new-email")
    public String verifyNewEmail(@RequestParam String code,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.verifyNewEmail(principal.getName(), code);
            redirectAttributes.addFlashAttribute("message", "Пошта успішно оновлена.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "verify-new-email";
        }
        return "redirect:/auth/login";
    }

    @PostMapping("/update-phone")
    public String updatePhone(@RequestParam String newPhone,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.updatePhoneNumber(principal.getName(), newPhone);
            redirectAttributes.addFlashAttribute("message", "Номер телефону успішно оновлено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/update-firstname")
    public String updateFirstName(@RequestParam String newFirstName,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.updateFirstName(principal.getName(), newFirstName);
            redirectAttributes.addFlashAttribute("message", "Ім'я успішно оновлено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/update-lastname")
    public String updateLastName(@RequestParam String newLastName,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.updateLastName(principal.getName(), newLastName);
            redirectAttributes.addFlashAttribute("message", "Прізвище успішно оновлено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String newPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.updatePassword(principal.getName(), newPassword);
            redirectAttributes.addFlashAttribute("message", "Пароль успішно оновлено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/delete")
    public String deleteProfile(Principal principal,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(principal.getName());
            request.getSession().invalidate();
            redirectAttributes.addFlashAttribute("message", "Ваш профіль успішно видалено.");
            return "redirect:/auth/login?deleted";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не вдалося видалити профіль: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}
