package uk.gov.hmcts.reform.locationrefdata.camel.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.CourtVenue;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CourtVenueMapperTest {

    CourtVenueMapper courtVenueMapper = spy(new CourtVenueMapper());

    @Test
    void testGetMap() {
        CourtVenue courtVenue = CourtVenue.builder()
            .epimmsId("123456")
            .siteName("Test Site")
            .courtName("Test Court Name")
            .courtStatus("Open")
            .courtOpenDate("12/12/12")
            .regionId("1")
            .courtTypeId("2")
            .clusterId("3")
            .openForPublic("Yes")
            .courtAddress("Test Court Address")
            .postcode("ABC 123")
            .phoneNumber("12343434")
            .closedDate("12/03/21")
            .courtLocationCode("12AB")
            .dxAddress("Test Dx Address")
            .welshSiteName("Test Welsh Site Name")
            .welshCourtAddress("Test Welsh Court Address")
            .venueName("Test Venue Name")
            .isCaseManagementLocation("Y")
            .isHearingLocation("N")
            .welshVenueName("testVenue")
            .isTemporaryLocation("N")
            .isNightingaleCourt("N")
            .locationType("Court")
            .parentLocation("366559")
            .welshCourtName("testWelshCourtName")
            .uprn("uprn123")
            .venueOuCode("venueOuCode1")
            .mrdBuildingLocationId("mrdBId1")
            .mrdVenueId("mrdVenueId1")
            .serviceUrl("serviceUrl1")
            .factUrl("factUrl1")
            .externalShortName("External Court")
            .welshExternalShortName("WelshExternalShortName")
            .serviceCode("serviceCode1")
            .build();

        var expectedMap = new HashMap<String, Object>();
        expectedMap.put("epimms_id", "123456");
        expectedMap.put("site_name", "Test Site");
        expectedMap.put("court_name", "Test Court Name");
        expectedMap.put("court_status", "Open");
        expectedMap.put("court_open_date", "12/12/12");
        expectedMap.put("region_id", "1");
        expectedMap.put("court_type_id", "2");
        expectedMap.put("cluster_id", "3");
        expectedMap.put("open_for_public", "Yes");
        expectedMap.put("court_address", "Test Court Address");
        expectedMap.put("postcode", "ABC 123");
        expectedMap.put("phone_number", "12343434");
        expectedMap.put("closed_date", "12/03/21");
        expectedMap.put("court_location_code", "12AB");
        expectedMap.put("dx_address", "Test Dx Address");
        expectedMap.put("welsh_site_name", "Test Welsh Site Name");
        expectedMap.put("welsh_court_address", "Test Welsh Court Address");
        expectedMap.put("venue_name", "Test Venue Name");
        expectedMap.put("is_case_management_location", "Y");
        expectedMap.put("is_hearing_location", "N");
        expectedMap.put("welsh_venue_name", "testVenue");
        expectedMap.put("is_temporary_location", "N");
        expectedMap.put("is_nightingale_court", "N");
        expectedMap.put("location_type", "Court");
        expectedMap.put("parent_location", "366559");
        expectedMap.put("welsh_court_name", "testWelshCourtName");
        expectedMap.put("uprn", "uprn123");
        expectedMap.put("venue_ou_code", "venueOuCode1");
        expectedMap.put("mrd_building_location_id", "mrdBId1");
        expectedMap.put("mrd_venue_id", "mrdVenueId1");
        expectedMap.put("service_url", "serviceUrl1");
        expectedMap.put("fact_url", "factUrl1");
        expectedMap.put("mrd_created_time",null);
        expectedMap.put("mrd_updated_time",null);
        expectedMap.put("mrd_deleted_time",null);
        expectedMap.put("external_short_name","External Court");
        expectedMap.put("welsh_external_short_name","WelshExternalShortName");
        expectedMap.put("service_code","serviceCode1");
        var actualMap = courtVenueMapper.getMap(courtVenue);

        assertEquals(actualMap, expectedMap);

        verify(courtVenueMapper, times(1)).getMap(courtVenue);
    }

}
