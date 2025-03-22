package ua.rzs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.rzs.model.ServiceItem;

public interface ServiceRepository extends JpaRepository<ServiceItem, Long> {
}

