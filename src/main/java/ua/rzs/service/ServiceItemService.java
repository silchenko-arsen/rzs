package ua.rzs.service;

import ua.rzs.model.ServiceItem;

import java.util.List;

public interface ServiceItemService {
    List<ServiceItem> findAll();

    ServiceItem findById(Long id);

    void save(ServiceItem serviceItem);

    void deleteById(Long id);
}
