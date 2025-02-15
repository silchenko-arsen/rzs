package ua.rzs.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.rzs.model.User;
import ua.rzs.repository.UserRepository;

@RequestMapping("/auth")
@Controller
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // 📌 Відображення сторінки реєстрації
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Повертає register.html
    }

    // 📌 Обробка реєстрації
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid User user,
                               BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register"; // Повертає форму з помилками
        }

        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            model.addAttribute("error", "Користувач з таким номером вже існує!");
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword())); // Хешування пароля
        user.setEnabled(true); // Вмикаємо акаунт після реєстрації
        userRepository.save(user);

        model.addAttribute("success", "Реєстрація успішна! Увійдіть в систему.");
        return "login"; // Перенаправлення на сторінку входу
    }

    // 📌 Відображення сторінки логіну
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // 📌 Обробка логіну (Spring Security обробляє POST /perform_login)
    @PostMapping("/perform_login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            Model model) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "redirect:/dashboard";
    }

    // 📌 Вихід з системи
    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/auth/login?logout"; // Перенаправлення на сторінку входу
    }
}
