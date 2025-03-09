package ua.rzs.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.rzs.model.User;
import ua.rzs.service.UserService;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid User user, Model model) {
        try {
            userService.save(user);
            model.addAttribute("email", user.getEmail());
            return "verify";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify")
    public String showVerificationPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verify";
    }

    @PostMapping("/verify")
    public String verifyCode(@RequestParam String email, @RequestParam String code, Model model) {
        try {
            if (userService.verifyCode(email, code)) {
                return "redirect:/auth/login";
            }
            model.addAttribute("email", email);
            model.addAttribute("error", "Ви ввели неправильний код.");
            return "verify";
        } catch (IllegalStateException e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "verify";
        }
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/perform_login")
    public String performLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        if (userService.login(username, password)) {
            return "redirect:/home";
        } else {
            return "redirect:/auth/login?error";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        try {
            userService.initiatePasswordReset(email);
            model.addAttribute("message", "Лист для відновлення паролю надіслано на вашу пошту.");
            model.addAttribute("email", email);
            return "reset-password";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email,
                                       @RequestParam String code,
                                       @RequestParam String newPassword,
                                       Model model) {
        try {
            if (userService.resetPassword(email, code, newPassword)) {
                model.addAttribute("message", "Пароль успішно змінено.");
                return "redirect:/auth/login";
            }
            model.addAttribute("email", email);
            model.addAttribute("error", "Ви ввели неправильний код.");
            return "/reset-password";
        } catch (IllegalStateException e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "/reset-password";
        }
    }
}

