package ua.rzs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.rzs.service.UserService;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        model.addAttribute("user", userService.findByEmail(principal.getName()));
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
}
