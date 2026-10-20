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
    IF EXISTS (
        SELECT 1
        FROM court_venue
        WHERE mrd_venue_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Cannot make mrd_venue_id primary key: null values exist';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM court_venue
        GROUP BY mrd_venue_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot make mrd_venue_id primary key: duplicate values exist';
    END IF;
END $$;

ALTER TABLE court_district_family_jurisdiction_assoc
    DROP CONSTRAINT IF EXISTS dfj_court_location_id_fk;

ALTER TABLE court_district_civil_jurisdiction_assoc
    DROP CONSTRAINT IF EXISTS dcj_court_location_id_fk;

ALTER TABLE court_venue
    DROP CONSTRAINT IF EXISTS court_location_unique;

ALTER TABLE court_venue
    DROP CONSTRAINT IF EXISTS court_id_pk;

ALTER TABLE court_venue
    DROP CONSTRAINT IF EXISTS court_venue_pkey;

ALTER TABLE court_venue
    DROP CONSTRAINT IF EXISTS court_venue_pk;

ALTER TABLE court_venue
    ALTER COLUMN mrd_venue_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'court_venue_id_uq'
          AND conrelid = 'court_venue'::regclass
    ) THEN
        ALTER TABLE court_venue
            ADD CONSTRAINT court_venue_id_uq UNIQUE (court_venue_id);
    END IF;
END $$;

ALTER TABLE court_venue
    ADD CONSTRAINT court_venue_pk PRIMARY KEY (mrd_venue_id);

ALTER TABLE court_district_family_jurisdiction_assoc
    ADD CONSTRAINT dfj_court_location_id_fk
        FOREIGN KEY (court_location_id)
            REFERENCES court_venue (court_venue_id);

ALTER TABLE court_district_civil_jurisdiction_assoc
    ADD CONSTRAINT dcj_court_location_id_fk
        FOREIGN KEY (court_location_id)
            REFERENCES court_venue (court_venue_id);

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

INSERT INTO court_name_type (court_name_type, court_name_type_desc)
VALUES
    ('SITE', 'Site'),
    ('COURT', 'Court'),
    ('VENUE', 'Venue'),
    ('EXTERNAL_SHORT', 'External Short'),
    ('DISTRICT_REGISTRY_SITE', 'District Registry Site'),
    ('DISTRICT_REGISTRY_EXTERNAL_SHORT', 'District Registry External Short')
ON CONFLICT (court_name_type) DO UPDATE
SET court_name_type_desc = EXCLUDED.court_name_type_desc;

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
FROM court_status;

CREATE OR REPLACE VIEW rdlocationreport.vw_reference_codes AS
SELECT mrd_venue_id, reference_code_type, reference_code
FROM reference_codes;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_name_type AS
SELECT court_name_type, court_name_type_desc
FROM court_name_type;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_venue_name AS
SELECT mrd_venue_id, court_name_type, language_code, name_desc
FROM court_venue_name;

CREATE OR REPLACE VIEW rdlocationreport.vw_address AS
SELECT mrd_venue_id, address_type, language_code, address, post_code, uprn
FROM address;

CREATE OR REPLACE VIEW rdlocationreport.vw_contact_method AS
SELECT contact_method_code, language_code, contact_method_desc
FROM contact_method;

CREATE OR REPLACE VIEW rdlocationreport.vw_contact_type AS
SELECT contact_type_code, language_code, contact_type_desc
FROM contact_type;

CREATE OR REPLACE VIEW rdlocationreport.vw_contact_details AS
SELECT mrd_venue_id, contact_method_code, contact_type_code, contact_value
FROM contact_details;

CREATE OR REPLACE VIEW rdlocationreport.vw_use_type AS
SELECT use_type_code, language_code, use_type_desc
FROM use_type;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_use_mapping AS
SELECT mrd_venue_id, use_type_code
FROM court_use_mapping;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_venue_url AS
SELECT mrd_venue_id, url_type, url
FROM court_venue_url;

CREATE OR REPLACE VIEW rdlocationreport.vw_court_venue AS
SELECT site_en.name_desc AS site_name, cv.region_id, cv.court_type_id, cv.cluster_id, cv.open_for_public,
       addr_en.address AS court_address, addr_en.post_code AS postcode,
       phone.contact_value::VARCHAR(128) AS phone_number,
       cv.closed_date, cv.court_location_code, cv.dx_address,
       site_cy.name_desc AS welsh_site_name, addr_cy.address AS welsh_court_address, cv.court_status,
       cv.court_open_date,
       court_en.name_desc AS court_name, venue_en.name_desc AS venue_name, cv.is_case_management_location,
       cv.is_hearing_location, venue_cy.name_desc AS welsh_venue_name, cv.is_temporary_location,
       cv.is_nightingale_court, cv.location_type, cv.parent_location, court_cy.name_desc AS welsh_court_name,
       addr_en.uprn, cv.venue_ou_code, cv.mrd_building_location_id, cv.mrd_venue_id, cv.service_url, cv.fact_url,
       ext_en.name_desc AS external_short_name, ext_cy.name_desc AS welsh_external_short_name, cv.service_code,
       cv.court_status_code, cv.open_date, cv.parent_id, cv.district_registry_venue_id, cv.appeal_centre_venue_id,
       cv.effective_from_date, cv.effective_to_date,
       dr_site_en.name_desc AS district_registry_site_name,
       dr_site_cy.name_desc AS district_registry_welsh_site_name,
       dr_ext_en.name_desc AS district_registry_external_short_name,
       dr_ext_cy.name_desc AS district_registry_welsh_external_short_name,
       contact_email.contact_value::VARCHAR(256) AS contact_email,
       breathing_space.contact_value::VARCHAR(256) AS breathing_space_email,
       cv.is_district_registry, cv.is_appeal_centre,
       cv.mrd_created_time, cv.mrd_updated_time, cv.mrd_deleted_time
FROM court_venue cv
LEFT JOIN address addr_en
    ON addr_en.mrd_venue_id = cv.mrd_venue_id
   AND addr_en.address_type = 'MAILING'
   AND addr_en.language_code = 'EN'
LEFT JOIN address addr_cy
    ON addr_cy.mrd_venue_id = cv.mrd_venue_id
   AND addr_cy.address_type = 'MAILING'
   AND addr_cy.language_code = 'CY'
LEFT JOIN contact_details phone
    ON phone.mrd_venue_id = cv.mrd_venue_id
   AND phone.contact_method_code = 'PHONE'
   AND phone.contact_type_code = 'CONTACT_SERVICE'
LEFT JOIN contact_details contact_email
    ON contact_email.mrd_venue_id = cv.mrd_venue_id
   AND contact_email.contact_method_code = 'EMAIL'
   AND contact_email.contact_type_code = 'CONTACT_SERVICE'
LEFT JOIN contact_details breathing_space
    ON breathing_space.mrd_venue_id = cv.mrd_venue_id
   AND breathing_space.contact_method_code = 'EMAIL'
   AND breathing_space.contact_type_code = 'BREATHING_SPACE'
LEFT JOIN court_venue_name site_en
    ON site_en.mrd_venue_id = cv.mrd_venue_id
   AND site_en.court_name_type = 'SITE'
   AND site_en.language_code = 'EN'
LEFT JOIN court_venue_name site_cy
    ON site_cy.mrd_venue_id = cv.mrd_venue_id
   AND site_cy.court_name_type = 'SITE'
   AND site_cy.language_code = 'CY'
LEFT JOIN court_venue_name court_en
    ON court_en.mrd_venue_id = cv.mrd_venue_id
   AND court_en.court_name_type = 'COURT'
   AND court_en.language_code = 'EN'
LEFT JOIN court_venue_name court_cy
    ON court_cy.mrd_venue_id = cv.mrd_venue_id
   AND court_cy.court_name_type = 'COURT'
   AND court_cy.language_code = 'CY'
LEFT JOIN court_venue_name venue_en
    ON venue_en.mrd_venue_id = cv.mrd_venue_id
   AND venue_en.court_name_type = 'VENUE'
   AND venue_en.language_code = 'EN'
LEFT JOIN court_venue_name venue_cy
    ON venue_cy.mrd_venue_id = cv.mrd_venue_id
   AND venue_cy.court_name_type = 'VENUE'
   AND venue_cy.language_code = 'CY'
LEFT JOIN court_venue_name ext_en
    ON ext_en.mrd_venue_id = cv.mrd_venue_id
   AND ext_en.court_name_type = 'EXTERNAL_SHORT'
   AND ext_en.language_code = 'EN'
LEFT JOIN court_venue_name ext_cy
    ON ext_cy.mrd_venue_id = cv.mrd_venue_id
   AND ext_cy.court_name_type = 'EXTERNAL_SHORT'
   AND ext_cy.language_code = 'CY'
LEFT JOIN court_venue_name dr_site_en
    ON dr_site_en.mrd_venue_id = cv.mrd_venue_id
   AND dr_site_en.court_name_type = 'DISTRICT_REGISTRY_SITE'
   AND dr_site_en.language_code = 'EN'
LEFT JOIN court_venue_name dr_site_cy
    ON dr_site_cy.mrd_venue_id = cv.mrd_venue_id
   AND dr_site_cy.court_name_type = 'DISTRICT_REGISTRY_SITE'
   AND dr_site_cy.language_code = 'CY'
LEFT JOIN court_venue_name dr_ext_en
    ON dr_ext_en.mrd_venue_id = cv.mrd_venue_id
   AND dr_ext_en.court_name_type = 'DISTRICT_REGISTRY_EXTERNAL_SHORT'
   AND dr_ext_en.language_code = 'EN'
LEFT JOIN court_venue_name dr_ext_cy
    ON dr_ext_cy.mrd_venue_id = cv.mrd_venue_id
   AND dr_ext_cy.court_name_type = 'DISTRICT_REGISTRY_EXTERNAL_SHORT'
   AND dr_ext_cy.language_code = 'CY';
