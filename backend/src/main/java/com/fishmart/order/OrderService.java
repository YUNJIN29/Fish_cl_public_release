package com.fishmart.order;

import com.fishmart.catalog.Product;
import com.fishmart.catalog.ProductRepository;
import com.fishmart.catalog.ProductStatus;
import com.fishmart.common.BizException;
import com.fishmart.order.dto.CreateOrderRequest;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
  private final OrderRepository orderRepo;
  private final ProductRepository productRepo;
  private final UserRepository userRepo;

  public OrderService(OrderRepository orderRepo, ProductRepository productRepo, UserRepository userRepo) {
    this.orderRepo = orderRepo;
    this.productRepo = productRepo;
    this.userRepo = userRepo;
  }

  @Transactional
  public Order create(Long userId, CreateOrderRequest req) {
    User user = userRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
    if (req.getItems() == null || req.getItems().isEmpty()) throw new BizException("订单商品不能为空");

    // 合并相同商品
    Map<Long, Integer> qtyMap = new HashMap<>();
    for (CreateOrderRequest.Item it : req.getItems()) {
      qtyMap.merge(it.getProductId(), it.getQuantity(), Integer::sum);
    }

    Order order = new Order();
    order.setUser(user);
    order.setOrderNo(genOrderNo());
    order.setStatus(OrderStatus.PENDING);

    int total = 0;
    for (Map.Entry<Long, Integer> e : qtyMap.entrySet()) {
      Long productId = e.getKey();
      int qty = e.getValue();
      Product p = productRepo.findById(productId).orElseThrow(() -> new BizException("商品不存在: " + productId));
      if (p.getStatus() != ProductStatus.ON) throw new BizException("商品已下架: " + p.getName());
      if (p.getStock() < qty) throw new BizException("库存不足: " + p.getName());

      // 扣库存
      p.setStock(p.getStock() - qty);
      productRepo.save(p);

      OrderItem oi = new OrderItem();
      oi.setOrder(order);
      oi.setProduct(p);
      oi.setQuantity(qty);
      oi.setUnitPriceCents(p.getPriceCents());
      order.getItems().add(oi);

      total += p.getPriceCents() * qty;
    }
    order.setTotalCents(total);
    return orderRepo.save(order);
  }

  public Page<Order> myOrders(Long userId, Pageable pageable) {
    return orderRepo.findByUserIdOrderByIdDesc(userId, pageable);
  }

  public Order getMyOrder(Long userId, Long orderId) {
    Order o = orderRepo.findById(orderId).orElseThrow(() -> new BizException("订单不存在"));
    if (!o.getUser().getId().equals(userId)) throw new BizException("无权访问该订单");
    return o;
  }

  public Page<Order> adminList(Pageable pageable) {
    return orderRepo.findAll(pageable);
  }

  @Transactional
  public Order adminUpdateStatus(Long orderId, OrderStatus status) {
    Order o = orderRepo.findById(orderId).orElseThrow(() -> new BizException("订单不存在"));
    o.setStatus(status);
    return orderRepo.save(o);
  }

  private String genOrderNo() {
    String ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Shanghai")).format(Instant.now());
    String rnd = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    return "FM" + ts + rnd;
  }
}

