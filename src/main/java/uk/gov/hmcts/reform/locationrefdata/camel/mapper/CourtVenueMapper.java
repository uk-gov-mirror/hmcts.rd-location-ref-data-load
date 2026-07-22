package uk.gov.hmcts.reform.locationrefdata.camel.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.mapper.IMapper;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.CourtVenue;

import java.util.Map;

import static uk.gov.hmcts.reform.locationrefdata.camel.util.LrdLoadUtils.trim;

@Component
public class CourtVenueMapper implements IMapper {

    @Override
    public Map<String, Object> getMap(Object courtVenue) {
        CourtVenue courtVenueType = (CourtVenue) courtVenue;

        Map<String, Object> courtVenueRow = CommonMapper.getMap(courtVenueType.getEpimmsId(),
                                                                courtVenueType.getRegionId(),
                                                                courtVenueType.getClusterId(),
                                                                courtVenueType.getPostcode(),
                                                                courtVenueType.getUprn(),
                                                                courtVenueType.getMrdBuildingLocationId(),
                                                                courtVenueType.getMrdCreatedTime(),
                                                                courtVenueType.getMrdUpdatedTime(),
                                                                courtVenueType.getMrdDeletedTime());

        courtVenueRow.put("site_name", trim(courtVenueType.getSiteName()));
        courtVenueRow.put("court_name", trim(courtVenueType.getCourtName()));
        courtVenueRow.put("court_status", trim(courtVenueType.getCourtStatus()));
        courtVenueRow.put("court_open_date", courtVenueType.getCourtOpenDate());
        courtVenueRow.put("court_type_id", courtVenueType.getCourtTypeId());
        courtVenueRow.put("open_for_public", courtVenueType.getOpenForPublic());
        courtVenueRow.put("court_address", trim(courtVenueType.getCourtAddress()));
        courtVenueRow.put("postcode", trim(courtVenueType.getPostcode()));
        courtVenueRow.put("phone_number", trim(courtVenueType.getPhoneNumber()));
        courtVenueRow.put("closed_date", courtVenueType.getClosedDate());
        courtVenueRow.put("court_location_code", trim(courtVenueType.getCourtLocationCode()));
        courtVenueRow.put("dx_address", trim(courtVenueType.getDxAddress()));
        courtVenueRow.put("welsh_site_name", trim(courtVenueType.getWelshSiteName()));
        courtVenueRow.put("welsh_court_address", trim(courtVenueType.getWelshCourtAddress()));
        courtVenueRow.put("venue_name", trim(courtVenueType.getVenueName()));
        courtVenueRow.put("is_case_management_location", trim(courtVenueType.getIsCaseManagementLocation()));
        courtVenueRow.put("is_hearing_location", trim(courtVenueType.getIsHearingLocation()));
        courtVenueRow.put("welsh_venue_name", trim(courtVenueType.getWelshVenueName()));
        courtVenueRow.put("is_temporary_location", trim(courtVenueType.getIsTemporaryLocation()));
        courtVenueRow.put("is_nightingale_court", trim(courtVenueType.getIsNightingaleCourt()));
        courtVenueRow.put("location_type", trim(courtVenueType.getLocationType()));
        courtVenueRow.put("parent_location", trim(courtVenueType.getParentLocation()));
        courtVenueRow.put("welsh_court_name", trim(courtVenueType.getWelshCourtName()));
        courtVenueRow.put("venue_ou_code", trim(courtVenueType.getVenueOuCode()));
        courtVenueRow.put("mrd_venue_id", trim(courtVenueType.getMrdVenueId()));
        courtVenueRow.put("service_url", trim(courtVenueType.getServiceUrl()));
        courtVenueRow.put("fact_url", trim(courtVenueType.getFactUrl()));
        courtVenueRow.put("external_short_name", trim(courtVenueType.getExternalShortName()));
        courtVenueRow.put("welsh_external_short_name", trim(courtVenueType.getWelshExternalShortName()));
        courtVenueRow.put("service_code", trim(courtVenueType.getServiceCode()));
        return courtVenueRow;
    }
}
