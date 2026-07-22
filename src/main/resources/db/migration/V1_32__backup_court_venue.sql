-- Backup court_venue data before adding service_code composite key
DROP TABLE IF EXISTS court_venue_backup_2026;
CREATE TABLE court_venue_backup_2026 AS
SELECT * FROM court_venue;

-- Capture backup metadata for audit/recovery proof
CREATE TABLE IF NOT EXISTS court_venue_backup_meta (
  backup_taken_at TIMESTAMP NOT NULL,
  row_count BIGINT NOT NULL,
  seq_last_value BIGINT NOT NULL,
  seq_is_called BOOLEAN NOT NULL
);

INSERT INTO court_venue_backup_meta (backup_taken_at, row_count, seq_last_value, seq_is_called)
SELECT now(),
       (SELECT count(*) FROM court_venue),
       (SELECT last_value FROM court_venue_seq),
       (SELECT is_called FROM court_venue_seq);
