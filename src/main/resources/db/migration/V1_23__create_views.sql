-- view creation for cluster
 create view rdlocationreport.vw_cluster as
 select cluster_id, cluster_name from locrefdata.cluster;

-- view creation for court_type
 create view rdlocationreport.vw_court_type as
 select court_type_id, court_type, welsh_court_type
 from locrefdata.court_type;

-- view creation for building_location
 create view rdlocationreport.vw_building_location as
 select building_location_id, epimms_id, building_location_name,area, region_id,cluster_id, court_finder_url, postcode,
 address, building_location_status, welsh_building_location_name, welsh_address, uprn, latitude, longitude
 from locrefdata.building_location;

-- view creation for court_venue
 create view rdlocationreport.vw_court_venue as
 select site_name, region_id, court_type_id ,cluster_id ,open_for_public ,court_address ,postcode , phone_number,
 closed_date,court_location_code,dx_address, welsh_site_name,welsh_court_address, court_status, court_open_date,
 court_name ,venue_name,is_case_management_location ,is_hearing_location,welsh_venue_name,is_temporary_location,
 is_nightingale_court,location_type,parent_location,welsh_court_name,uprn,venue_ou_code,mrd_building_location_id,
 mrd_venue_id,service_url,fact_url from locrefdata.court_venue;

 -- view creation for court_type_service_assoc
create view rdlocationreport.vw_court_type_service_assoc as
select court_type_service_assoc_id, service_code,court_type_id
from locrefdata.court_type_service_assoc;
