package ua.rzs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.rzs.model.ServiceItem;
import ua.rzs.repository.ServiceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements ServiceItemService {
    private final ServiceRepository serviceRepository;


    @Override
    public List<ServiceItem> findAll() {
        return serviceRepository.findAll();
    }

    @Override
    public void save(ServiceItem serviceItem) {
        serviceRepository.save(serviceItem);
    }

    @Override
    public void deleteById(Long id) {
        serviceRepository.deleteById(id);
    }

    @Override
    public ServiceItem findById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Послугу не знайдено"));
    }
}
