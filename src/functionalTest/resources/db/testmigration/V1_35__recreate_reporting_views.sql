create or replace view rdlocationreport.vw_court_venue as
select site_name, region_id, court_type_id, cluster_id, open_for_public, court_address, postcode, phone_number,
       closed_date, court_location_code, dx_address, welsh_site_name, welsh_court_address, court_status,
       court_open_date, court_name, venue_name, is_case_management_location, is_hearing_location,
       welsh_venue_name, is_temporary_location, is_nightingale_court, location_type, parent_location,
       welsh_court_name, uprn, venue_ou_code, mrd_building_location_id, mrd_venue_id, service_url, fact_url,
       external_short_name, welsh_external_short_name, service_code
from court_venue;
