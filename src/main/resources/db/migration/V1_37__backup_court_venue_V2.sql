-- Backup court_venue data before adding service_code composite key
DROP TABLE IF EXISTS court_venue_V2_backup_2026;
CREATE TABLE court_venue_V2_backup_2026 AS
SELECT * FROM court_venue;
