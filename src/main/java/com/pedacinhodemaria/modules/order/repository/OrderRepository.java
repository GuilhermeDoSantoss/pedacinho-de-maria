package com.pedacinhodemaria.modules.order.repository;

import com.pedacinhodemaria.modules.order.domain.Order;
import com.pedacinhodemaria.modules.order.domain.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByStatusIn(Collection<OrderStatus> statuses);

    List<Order> findByStatusInOrderByCreatedAtAsc(Collection<OrderStatus> statuses);
}