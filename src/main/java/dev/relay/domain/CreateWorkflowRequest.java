package dev.relay.domain;
import jakarta.validation.constraints.*;
import java.util.*;
public record CreateWorkflowRequest(@NotBlank String workflowType, @NotBlank String idempotencyKey,
 @NotNull Map<String,Object> input, @NotEmpty List<Step> steps, @Min(1) @Max(20) Integer maxAttempts) {
 public record Step(@NotBlank String activity, Map<String,Object> config) {}
}
