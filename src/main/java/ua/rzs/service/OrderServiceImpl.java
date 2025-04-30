package ua.rzs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ua.rzs.model.Order;
import ua.rzs.model.OrderFile;
import ua.rzs.model.ServiceItem;
import ua.rzs.model.User;
import ua.rzs.repository.OrderFileRepository;
import ua.rzs.repository.OrderRepository;
import ua.rzs.repository.ServiceRepository;
import ua.rzs.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderFileRepository orderFileRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final DropboxService dropboxService;
    private final EmailService emailService;
    @Value("${app.payment.iban}")
    private String companyIban;

    @Override
    public void placeOrder(MultipartFile[] files, String comment, String userEmail, Long serviceItemId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Користувача не знайдено"));

        ServiceItem serviceItem = serviceRepository.findById(serviceItemId)
                .orElseThrow(() -> new IllegalStateException("Послугу не знайдено"));

        List<OrderFile> orderFiles = new ArrayList<>();

        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String dropboxPath = "/orders/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        String storedPath = dropboxService.uploadFile(file, dropboxPath);
                        OrderFile orderFile = OrderFile.builder()
                                .filePath(storedPath)
                                .fileName(file.getOriginalFilename())
                                .build();
                        orderFiles.add(orderFile);
                    } catch (Exception e) {
                        throw new IllegalStateException("Помилка при завантаженні файлу: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }

        Order order = Order.builder()
                .user(user)
                .serviceItem(serviceItem)
                .comment(comment)
                .status(Order.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .orderFiles(orderFiles)
                .build();

        orderFiles.forEach(orderFile -> orderFile.setOrder(order));

        orderRepository.save(order);
    }

    @Transactional
    public void updateStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        order.setStatus(newStatus);
        orderRepository.save(order);

        String userEmail = order.getUser().getEmail();
        if (newStatus == Order.OrderStatus.APPROVED) {
            emailService.sendIbanDetails(userEmail, companyIban, order);
        }
        else if (newStatus == Order.OrderStatus.REJECTED) {
            emailService.sendRejectionNotice(userEmail, order);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId);
    }

    @Override
    public ResponseEntity<Resource> findFileById(Long id) {
        OrderFile of = orderFileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Resource resource = dropboxService.downloadAsResource(of.getFilePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + of.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
