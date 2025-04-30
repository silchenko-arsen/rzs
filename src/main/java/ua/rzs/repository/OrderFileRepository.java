package ua.rzs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.rzs.model.OrderFile;

@Repository
public interface OrderFileRepository extends JpaRepository<OrderFile, Long> {
}
