create table campaign_daily_cycle_state (
    campaign_id uuid primary key,
    snapshot_json clob not null,
    updated_at timestamp not null
);
