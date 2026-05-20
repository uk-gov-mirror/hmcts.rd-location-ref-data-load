DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'locrefdata'
          AND table_name = 'court_venue'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'locrefdata'
          AND table_name = 'court_venue_backup_2026'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'locrefdata'
          AND table_name = 'court_venue'
          AND column_name = 'service_code'
    ) THEN
 TRUNCATE TABLE court_venue CASCADE;
END IF;
END $$;
