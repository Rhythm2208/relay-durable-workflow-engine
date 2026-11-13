package dev.relay.domain;
import java.time.Instant; import java.util.UUID;
public record Workflow(UUID id,String workflowType,String idempotencyKey,String input,WorkflowStatus status,int currentStep,int maxAttempts,long fencingToken,Instant createdAt,Instant updatedAt,Instant completedAt,String lastError) {}
