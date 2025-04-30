package ua.rzs.service;

import ua.rzs.model.User;

import java.util.List;

public interface UserService {
    void save(User user);

    boolean verifyCode(String email, String code);

    boolean login(String username, String password);

    void initiatePasswordReset(String email);

    boolean resetPassword(String email, String code, String newPassword);

    User findByEmail(String email);

    void initiateEmailUpdate(String currentEmail, String newEmail);

    void verifyNewEmail(String currentEmail, String code);

    void updatePhoneNumber(String currentEmail, String newPhoneNumber);

    void updateFirstName(String currentEmail, String newFirstName);

    void updateLastName(String currentEmail, String newLastName);

    void updatePassword(String currentEmail, String newPassword);

    void deleteUser(String email);

    List<User> findAllUsers();

    User findById(Long id);
}
