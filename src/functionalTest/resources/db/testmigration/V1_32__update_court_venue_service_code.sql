DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'locrefdata'
          AND table_name = 'court_venue'
          AND column_name = 'service_code'
    ) THEN
        UPDATE court_venue SET service_code = 'AAA2' WHERE court_venue_id = 1;
        UPDATE court_venue SET service_code = 'AAA3' WHERE court_venue_id = 2;
        UPDATE court_venue SET service_code = 'AAA6' WHERE court_venue_id = 3;
        UPDATE court_venue SET service_code = 'ABA4' WHERE court_venue_id = 4;
        UPDATE court_venue SET service_code = 'AAA2' WHERE court_venue_id = 5;
        UPDATE court_venue SET service_code = 'AAA3' WHERE court_venue_id = 6;
        UPDATE court_venue SET service_code = 'AAA6' WHERE court_venue_id = 7;
        UPDATE court_venue SET service_code = 'ABA4' WHERE court_venue_id = 8;
        UPDATE court_venue SET service_code = 'AAA2' WHERE court_venue_id = 9;
        UPDATE court_venue SET service_code = 'AAA3' WHERE court_venue_id = 10;
        UPDATE court_venue SET service_code = 'AAA6' WHERE court_venue_id = 11;
        UPDATE court_venue SET service_code = 'ABA4' WHERE court_venue_id = 12;
        UPDATE court_venue SET service_code = 'AAA2' WHERE court_venue_id = 13;
        UPDATE court_venue SET service_code = 'AAA3' WHERE court_venue_id = 14;
        UPDATE court_venue SET service_code = 'AAA6' WHERE court_venue_id = 15;
    END IF;
END $$;
