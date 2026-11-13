CREATE TABLE workflows (
 id UUID PRIMARY KEY, workflow_type VARCHAR(120) NOT NULL, idempotency_key VARCHAR(200) UNIQUE NOT NULL,
 input JSONB NOT NULL, status VARCHAR(24) NOT NULL, current_step INT NOT NULL DEFAULT 0,
 max_attempts INT NOT NULL DEFAULT 5, available_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 lease_owner VARCHAR(160), lease_until TIMESTAMPTZ, fencing_token BIGINT NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), completed_at TIMESTAMPTZ,
 last_error TEXT, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX workflows_claim_idx ON workflows(status, available_at, lease_until);
CREATE TABLE execution_history (
 id BIGSERIAL PRIMARY KEY, workflow_id UUID NOT NULL REFERENCES workflows(id), event_type VARCHAR(60) NOT NULL,
 step INT, attempt INT, worker_id VARCHAR(160), fencing_token BIGINT, details JSONB NOT NULL DEFAULT '{}'::jsonb,
 occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX history_workflow_idx ON execution_history(workflow_id,id);
CREATE TABLE workflow_steps (
 workflow_id UUID NOT NULL REFERENCES workflows(id), step_index INT NOT NULL, activity VARCHAR(120) NOT NULL,
 config JSONB NOT NULL DEFAULT '{}'::jsonb, PRIMARY KEY(workflow_id,step_index)
);
