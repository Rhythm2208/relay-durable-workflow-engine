package dev.relay.domain;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record CreateOrderRequest(@NotBlank @Size(max=100) String orderId,@NotBlank @Size(max=120) String customerName,@NotBlank @Email @Size(max=320) String email,@NotBlank String sku,@Min(1) @Max(20) int quantity,@NotNull @DecimalMin(value="0.01") @Digits(integer=10,fraction=2) BigDecimal amount,@NotNull PaymentScenario paymentScenario) { public enum PaymentScenario { APPROVE, FAIL_ONCE, DECLINE } }
