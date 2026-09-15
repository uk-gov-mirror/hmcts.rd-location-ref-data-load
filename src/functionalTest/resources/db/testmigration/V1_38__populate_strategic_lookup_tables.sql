INSERT INTO court_name_type (court_name_type, court_name_type_desc)
VALUES
    ('SITE', 'Site'),
    ('COURT', 'Court'),
    ('VENUE', 'Venue'),
    ('EXTERNAL_SHORT', 'External Short'),
    ('DISTRICT_REGISTRY_SITE', 'District Registry Site')
ON CONFLICT (court_name_type) DO UPDATE
SET court_name_type_desc = EXCLUDED.court_name_type_desc;

INSERT INTO court_type (
    court_type_id,
    court_type,
    welsh_court_type,
    created_time,
    updated_time,
    mrd_created_time,
    mrd_updated_time,
    mrd_deleted_time
)
VALUES
    ('1', 'Admin Court', NULL, NOW() AT TIME ZONE 'utc', NOW() AT TIME ZONE 'utc', '2022-04-01 02:00:00', '2022-04-01 02:00:00', NULL),
    ('2', 'Admiralty and Commercial Court', NULL, NOW() AT TIME ZONE 'utc', NOW() AT TIME ZONE 'utc', '2022-04-01 02:00:00', '2022-04-01 02:00:00', NULL),
    ('3', 'Agricultural Land and Drainage Tribunal', NULL, NOW() AT TIME ZONE 'utc', NOW() AT TIME ZONE 'utc', '2022-04-01 02:00:00', '2022-04-01 02:00:00', NULL),
    ('4', 'Asylum Support Appeals', NULL, NOW() AT TIME ZONE 'utc', NOW() AT TIME ZONE 'utc', '2022-04-01 02:00:00', '2022-04-01 02:00:00', NULL),
    ('5', 'Bankruptcy Court (High Court)', NULL, NOW() AT TIME ZONE 'utc', NOW() AT TIME ZONE 'utc', '2022-04-01 02:00:00', '2022-04-01 02:00:00', NULL)
ON CONFLICT (court_type_id) DO UPDATE
SET court_type = EXCLUDED.court_type,
    welsh_court_type = EXCLUDED.welsh_court_type,
    updated_time = EXCLUDED.updated_time,
    mrd_updated_time = EXCLUDED.mrd_updated_time,
    mrd_deleted_time = EXCLUDED.mrd_deleted_time;

INSERT INTO contact_type (contact_type_code, language_code, contact_type_desc)
VALUES
    ('CONTACT_SERVICE', 'EN', 'Contact Service'),
    ('BREATHING_SPACE', 'EN', 'Breathing Space'),
    ('GENERAL', 'EN', 'General Enquiry'),
    ('ADMIN', 'EN', 'Administration'),
    ('SUPPORT', 'EN', 'Support Enquiry')
ON CONFLICT (contact_type_code) DO UPDATE
SET language_code = EXCLUDED.language_code,
    contact_type_desc = EXCLUDED.contact_type_desc;

INSERT INTO use_type (use_type_code, language_code, use_type_desc)
VALUES
    ('CASE_MANAGEMENT', 'EN', 'Case Management Location'),
    ('HEARING', 'EN', 'Hearing Location'),
    ('TEMPORARY', 'EN', 'Temporary Location'),
    ('NIGHTINGALE', 'EN', 'Nightingale Court'),
    ('DISTRICT_REGISTRY', 'EN', 'District Registry')
ON CONFLICT (use_type_code) DO UPDATE
SET language_code = EXCLUDED.language_code,
    use_type_desc = EXCLUDED.use_type_desc;
