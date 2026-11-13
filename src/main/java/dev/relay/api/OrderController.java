package dev.relay.api;
import dev.relay.domain.*;import dev.relay.domain.OrderFulfillmentService.OrderRow;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/v1") public class OrderController {
 private final OrderFulfillmentService service;private final JdbcTemplate jdbc;public OrderController(OrderFulfillmentService s,JdbcTemplate j){service=s;jdbc=j;}
 @PostMapping("/orders") public ResponseEntity<Workflow> create(@Valid @RequestBody CreateOrderRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));}
 @GetMapping("/orders") public List<OrderRow> orders(){return service.dashboardOrders();}
 @GetMapping("/catalog") public List<Map<String,Object>> catalog(){return jdbc.queryForList("select sku,product_name,available from inventory_items order by product_name");}
}
