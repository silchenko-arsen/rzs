package ua.rzs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.rzs.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Role.RoleName name);
}
