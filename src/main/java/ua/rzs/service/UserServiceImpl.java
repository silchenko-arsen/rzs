package ua.rzs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.rzs.exception.EntityNotFoundException;
import ua.rzs.model.Role;
import ua.rzs.model.User;
import ua.rzs.repository.UserRepository;
import ua.rzs.repository.RoleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final Random random = new Random();

    @Override
    public void save(User user) {
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            User oldUser = optionalUser.get();
            if (oldUser.isEnabled()) {
                throw new IllegalStateException("Користувач з таким email вже існує!");
            } else {
                String code = generateVerificationCode();
                oldUser.setVerificationCode(code);
                oldUser.setVerificationExpiry(LocalDateTime.now().plusMinutes(60));
                userRepository.save(oldUser);
                sendVerificationEmail(oldUser.getEmail(), code);
            }
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setEnabled(false);
            String code = generateVerificationCode();
            user.setVerificationCode(code);
            user.setVerificationExpiry(LocalDateTime.now().plusHours(1));
            user.setRoles(Set.of(roleRepository.findByName(Role.RoleName.USER)
                    .orElseThrow(() -> new EntityNotFoundException("Can't find role by role name"
                            + Role.RoleName.USER.name()))));
            userRepository.save(user);
            sendVerificationEmail(user.getEmail(), code);
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationExpiry() != null && LocalDateTime.now().isAfter(user.getVerificationExpiry())) {
                String newCode = generateVerificationCode();
                user.setVerificationCode(newCode);
                user.setVerificationExpiry(LocalDateTime.now().plusHours(1));
                userRepository.save(user);
                sendVerificationEmail(user.getEmail(), newCode);
                throw new IllegalStateException("Час дії коду минув. Вам на пошту відправлений новий код.");
            }
            if (user.getVerificationCode().equals(code)) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationExpiry(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean login(String username, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user.isEnabled();
            }
        }
        return false;
    }

    @Override
    public void initiatePasswordReset(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String code = generateVerificationCode();
            user.setPasswordResetCode(code);
            user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            sendVerificationEmail(user.getEmail(), code);
        } else {
            throw new IllegalStateException("Користувача з такою поштою не знайдено.");
        }
    }

    @Override
    public boolean resetPassword(String email, String code, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPasswordResetExpiry() != null && LocalDateTime.now().isAfter(user.getPasswordResetExpiry())) {
                String newCode = generateVerificationCode();
                user.setPasswordResetCode(newCode);
                user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
                userRepository.save(user);
                sendVerificationEmail(user.getEmail(), newCode);
                throw new IllegalStateException("Час дії коду минув. Вам на пошту відправлений новий код.");
            }
            if (user.getPasswordResetCode().equals(code)) {
                user.setPasswordResetCode(null);
                user.setPasswordResetExpiry(null);
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Користувача не знайдено"));
    }

    @Override
    public void initiateEmailUpdate(String currentEmail, String newEmail) {
        User user = findByEmail(currentEmail);
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalStateException("Користувач з такою поштою вже існує");
        }
        String code = generateVerificationCode();
        user.setPendingEmail(newEmail);
        user.setEmailVerificationCode(code);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        sendVerificationEmail(newEmail, code);
    }

    @Override
    public void verifyNewEmail(String currentEmail, String code) {
        User user = findByEmail(currentEmail);
        if (user.getEmailVerificationCode() == null) {
            throw new IllegalStateException("Код не був надісланий");
        }
        if (!user.getEmailVerificationCode().equals(code)) {
            throw new IllegalStateException("Невірний код підтвердження");
        }
        if (user.getEmailVerificationExpiry() == null || LocalDateTime.now().isAfter(user.getEmailVerificationExpiry())) {
            throw new IllegalStateException("Термін дії коду сплив");
        }
        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setEmailVerificationCode(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);
    }

    @Override
    public void updatePhoneNumber(String currentEmail, String newPhoneNumber) {
        User user = findByEmail(currentEmail);
        user.setPhoneNumber(newPhoneNumber);
        userRepository.save(user);
    }

    @Override
    public void updateFirstName(String currentEmail, String newFirstName) {
        User user = findByEmail(currentEmail);
        user.setFirstName(newFirstName);
        userRepository.save(user);
    }

    @Override
    public void updateLastName(String currentEmail, String newLastName) {
        User user = findByEmail(currentEmail);
        user.setLastName(newLastName);
        userRepository.save(user);
    }

    @Override
    public void updatePassword(String currentEmail, String newPassword) {
        User user = findByEmail(currentEmail);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteUser(String email) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("Користувача не знайдено"));
            userRepository.delete(user);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Користувача не знайдено"));
    }

    private String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendVerificationEmail(String email, String code) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Код підтвердження");
        mailMessage.setText("Ваш код підтвердження: " + code);
        mailSender.send(mailMessage);
    }
}



