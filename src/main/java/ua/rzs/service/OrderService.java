package ua.rzs.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ua.rzs.model.Order;
import ua.rzs.model.OrderFile;

import java.util.List;

public interface OrderService {
    void placeOrder(MultipartFile[] files, String comment, String userEmail, Long serviceItemId);

    void updateStatus(Long id, Order.OrderStatus newStatus);

    List<Order> findByUserId(Long id);

    ResponseEntity<Resource> findFileById(Long id);
}

