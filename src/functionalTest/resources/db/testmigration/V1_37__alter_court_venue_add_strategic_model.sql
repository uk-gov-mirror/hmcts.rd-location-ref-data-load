ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS court_status_code VARCHAR(32);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS open_date DATE;
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS parent_id VARCHAR(16);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS district_registry_venue_id VARCHAR(16);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS appeal_centre_venue_id VARCHAR(16);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS effective_from_date DATE;
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS effective_to_date DATE;
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS district_registry_site_name VARCHAR(256);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS district_registry_welsh_site_name VARCHAR(256);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS district_registry_external_short_name VARCHAR(256);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS district_registry_welsh_external_short_name VARCHAR(256);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS contact_email VARCHAR(256);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS breathing_space_email VARCHAR(256);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS is_district_registry VARCHAR(1);
ALTER TABLE court_venue ADD COLUMN IF NOT EXISTS is_appeal_centre VARCHAR(1);

UPDATE court_venue
SET mrd_venue_id = NULL
WHERE mrd_venue_id = '';

UPDATE court_venue
SET open_date = court_open_date::date
WHERE open_date IS NULL
  AND court_open_date IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'court_venue_mrd_venue_id_uq'
    ) THEN
        ALTER TABLE court_venue
            ADD CONSTRAINT court_venue_mrd_venue_id_uq UNIQUE (mrd_venue_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'court_venue_parent_id_fk'
    ) THEN
        ALTER TABLE court_venue
            ADD CONSTRAINT court_venue_parent_id_fk FOREIGN KEY (parent_id)
                REFERENCES court_venue (mrd_venue_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'court_venue_district_registry_venue_id_fk'
    ) THEN
        ALTER TABLE court_venue
            ADD CONSTRAINT court_venue_district_registry_venue_id_fk FOREIGN KEY (district_registry_venue_id)
                REFERENCES court_venue (mrd_venue_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'court_venue_appeal_centre_venue_id_fk'
    ) THEN
        ALTER TABLE court_venue
            ADD CONSTRAINT court_venue_appeal_centre_venue_id_fk FOREIGN KEY (appeal_centre_venue_id)
                REFERENCES court_venue (mrd_venue_id);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS court_status (
    court_status_code VARCHAR(32) NOT NULL,
    language_code VARCHAR(2) NOT NULL DEFAULT 'EN',
    court_status_desc VARCHAR(256) NOT NULL,
    CONSTRAINT court_status_pk PRIMARY KEY (court_status_code)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'court_venue_court_status_code_fk'
    ) THEN
        ALTER TABLE court_venue
            ADD CONSTRAINT court_venue_court_status_code_fk FOREIGN KEY (court_status_code)
                REFERENCES court_status (court_status_code);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS reference_codes (
    mrd_venue_id VARCHAR(16) NOT NULL,
    reference_code_type VARCHAR(64) NOT NULL,
    reference_code VARCHAR(256) NOT NULL,
    CONSTRAINT reference_codes_pk PRIMARY KEY (mrd_venue_id, reference_code_type, reference_code),
    CONSTRAINT reference_codes_mrd_venue_id_fk FOREIGN KEY (mrd_venue_id)
        REFERENCES court_venue (mrd_venue_id)
);

CREATE TABLE IF NOT EXISTS court_venue_url (
    mrd_venue_id VARCHAR(16) NOT NULL,
    url_type VARCHAR(64) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    CONSTRAINT court_venue_url_pk PRIMARY KEY (mrd_venue_id, url_type),
    CONSTRAINT court_venue_url_mrd_venue_id_fk FOREIGN KEY (mrd_venue_id)
        REFERENCES court_venue (mrd_venue_id)
);

CREATE TABLE IF NOT EXISTS court_name_type (
    court_name_type VARCHAR(64) NOT NULL,
    court_name_type_desc VARCHAR(256) NOT NULL,
    CONSTRAINT court_name_type_pk PRIMARY KEY (court_name_type)
);

CREATE TABLE IF NOT EXISTS contact_method (
    contact_method_code VARCHAR(64) NOT NULL,
    language_code VARCHAR(2) NOT NULL DEFAULT 'EN',
    contact_method_desc VARCHAR(256) NOT NULL,
    CONSTRAINT contact_method_pk PRIMARY KEY (contact_method_code)
);

CREATE TABLE IF NOT EXISTS contact_type (
    contact_type_code VARCHAR(64) NOT NULL,
    language_code VARCHAR(2) NOT NULL DEFAULT 'EN',
    contact_type_desc VARCHAR(256) NOT NULL,
    CONSTRAINT contact_type_pk PRIMARY KEY (contact_type_code)
);

CREATE TABLE IF NOT EXISTS use_type (
    use_type_code VARCHAR(64) NOT NULL,
    language_code VARCHAR(2) NOT NULL DEFAULT 'EN',
    use_type_desc VARCHAR(256) NOT NULL,
    CONSTRAINT use_type_pk PRIMARY KEY (use_type_code)
);

CREATE TABLE IF NOT EXISTS court_venue_name (
    mrd_venue_id VARCHAR(16) NOT NULL,
    court_name_type VARCHAR(64) NOT NULL,
    language_code VARCHAR(2) NOT NULL DEFAULT 'EN',
    name_desc VARCHAR(256) NOT NULL,
    CONSTRAINT court_venue_name_pk PRIMARY KEY (mrd_venue_id, court_name_type, language_code),
    CONSTRAINT court_venue_name_mrd_venue_id_fk FOREIGN KEY (mrd_venue_id)
        REFERENCES court_venue (mrd_venue_id),
    CONSTRAINT court_venue_name_type_fk FOREIGN KEY (court_name_type)
        REFERENCES court_name_type (court_name_type)
);

CREATE TABLE IF NOT EXISTS address (
    mrd_venue_id VARCHAR(16) NOT NULL,
    address_type VARCHAR(64) NOT NULL,
    language_code VARCHAR(2) NOT NULL DEFAULT 'EN',
    address VARCHAR(512) NOT NULL,
    post_code VARCHAR(8),
    uprn VARCHAR(16),
    CONSTRAINT address_pk PRIMARY KEY (mrd_venue_id, address_type, language_code),
    CONSTRAINT address_mrd_venue_id_fk FOREIGN KEY (mrd_venue_id)
        REFERENCES court_venue (mrd_venue_id)
);

CREATE TABLE IF NOT EXISTS contact_details (
    mrd_venue_id VARCHAR(16) NOT NULL,
    contact_method_code VARCHAR(64) NOT NULL,
    contact_type_code VARCHAR(64) NOT NULL,
    contact_value VARCHAR(1024) NOT NULL,
    CONSTRAINT contact_details_pk PRIMARY KEY (mrd_venue_id, contact_method_code, contact_type_code),
    CONSTRAINT contact_details_mrd_venue_id_fk FOREIGN KEY (mrd_venue_id)
        REFERENCES court_venue (mrd_venue_id),
    CONSTRAINT contact_details_method_fk FOREIGN KEY (contact_method_code)
        REFERENCES contact_method (contact_method_code),
    CONSTRAINT contact_details_type_fk FOREIGN KEY (contact_type_code)
        REFERENCES contact_type (contact_type_code)
);

CREATE TABLE IF NOT EXISTS court_use_mapping (
    mrd_venue_id VARCHAR(16) NOT NULL,
    use_type_code VARCHAR(64) NOT NULL,
    CONSTRAINT court_use_mapping_pk PRIMARY KEY (mrd_venue_id, use_type_code),
    CONSTRAINT court_use_mapping_mrd_venue_id_fk FOREIGN KEY (mrd_venue_id)
        REFERENCES court_venue (mrd_venue_id),
    CONSTRAINT court_use_mapping_type_fk FOREIGN KEY (use_type_code)
        REFERENCES use_type (use_type_code)
);

CREATE OR REPLACE VIEW rdlocationreport.vw_court_status AS
SELECT court_status_code, language_code, court_status_desc
FROM locrefdata.court_status;

CREATE OR REPLACE VIEW rdlocationreport.vw_reference_codes AS
SELECT mrd_venue_id, reference_code_type, reference_code
FROM locrefdata.reference_codes;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_name_type AS
SELECT court_name_type, court_name_type_desc
FROM locrefdata.court_name_type;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_venue_name AS
SELECT mrd_venue_id, court_name_type, language_code, name_desc
FROM locrefdata.court_venue_name;

CREATE OR REPLACE VIEW rdlocationreport.vw_address AS
SELECT mrd_venue_id, address_type, language_code, address, post_code, uprn
FROM locrefdata.address;

CREATE OR REPLACE VIEW rdlocationreport.vw_contact_method AS
SELECT contact_method_code, language_code, contact_method_desc
FROM locrefdata.contact_method;

CREATE OR REPLACE VIEW rdlocationreport.vw_contact_type AS
SELECT contact_type_code, language_code, contact_type_desc
FROM locrefdata.contact_type;

CREATE OR REPLACE VIEW rdlocationreport.vw_contact_details AS
SELECT mrd_venue_id, contact_method_code, contact_type_code, contact_value
FROM locrefdata.contact_details;

CREATE OR REPLACE VIEW rdlocationreport.vw_use_type AS
SELECT use_type_code, language_code, use_type_desc
FROM locrefdata.use_type;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_use_mapping AS
SELECT mrd_venue_id, use_type_code
FROM locrefdata.court_use_mapping;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_venue_url AS
SELECT mrd_venue_id, url_type, url
FROM locrefdata.court_venue_url;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_venue AS
SELECT site_name, region_id, court_type_id, cluster_id, open_for_public, court_address, postcode, phone_number,
       closed_date, court_location_code, dx_address, welsh_site_name, welsh_court_address, court_status,
       court_open_date, court_name, venue_name, is_case_management_location, is_hearing_location,
       welsh_venue_name, is_temporary_location, is_nightingale_court, location_type, parent_location,
       welsh_court_name, uprn, venue_ou_code, mrd_building_location_id, mrd_venue_id, service_url, fact_url,
       external_short_name, welsh_external_short_name, service_code, court_status_code, open_date, parent_id,
       district_registry_venue_id, appeal_centre_venue_id, effective_from_date, effective_to_date,
       district_registry_site_name, district_registry_welsh_site_name, district_registry_external_short_name,
       district_registry_welsh_external_short_name, contact_email, breathing_space_email,
       is_district_registry, is_appeal_centre, mrd_created_time, mrd_updated_time, mrd_deleted_time
FROM locrefdata.court_venue;
