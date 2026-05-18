-- Add service_code column to court_venue table
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS service_code VARCHAR(16);

-- Drop the existing unique constraint on (epimms_id, court_type_id)
ALTER TABLE court_venue DROP CONSTRAINT IF EXISTS court_location_unique;

-- Add a new unique constraint with epimms_id and service_code
ALTER TABLE court_venue ADD CONSTRAINT court_location_unique UNIQUE (epimms_id, service_code);

-- Add foreign key constraint for service_code
ALTER TABLE court_venue ADD CONSTRAINT court_venue_service_code_fk FOREIGN KEY (service_code) REFERENCES SERVICE (service_code);
