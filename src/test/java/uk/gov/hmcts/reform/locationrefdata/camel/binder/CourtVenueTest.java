package uk.gov.hmcts.reform.locationrefdata.camel.binder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CourtVenueTest {

    @Test
    void testCourtVenueBuilder() {

        CourtVenue courtVenue = CourtVenue.builder()
            .courtName("courtName")
            .courtAddress("courtAddress")
            .welshCourtAddress("welshCourtAddress")
            .courtStatus("courtStatus")
            .courtTypeId("1")
            .dxAddress("dxAddress")
            .courtLocationCode("courtLocationCode")
            .courtOpenDate("courtOpenDate")
            .closedDate("closedDate")
            .clusterId("2")
            .epimmsId("epimmsId")
            .openForPublic("openForPublic")
            .phoneNumber("phoneNumber")
            .postcode("postcode")
            .welshSiteName("welshSiteName")
            .siteName("siteName")
            .regionId("3")
            .venueName("venueName")
            .isCaseManagementLocation("Y")
            .isHearingLocation("N")
            .welshExternalShortName("Welsh External Court")
            .serviceCode("serviceCode1")
            .build();

        assertEquals("courtName", courtVenue.getCourtName());
        assertEquals("courtAddress", courtVenue.getCourtAddress());
        assertEquals("welshCourtAddress", courtVenue.getWelshCourtAddress());
        assertEquals("courtStatus", courtVenue.getCourtStatus());
        assertEquals("1", courtVenue.getCourtTypeId());
        assertEquals("dxAddress", courtVenue.getDxAddress());
        assertEquals("courtLocationCode", courtVenue.getCourtLocationCode());
        assertEquals("courtOpenDate", courtVenue.getCourtOpenDate());
        assertEquals("closedDate", courtVenue.getClosedDate());
        assertEquals("2", courtVenue.getClusterId());
        assertEquals("epimmsId", courtVenue.getEpimmsId());
        assertEquals("openForPublic", courtVenue.getOpenForPublic());
        assertEquals("phoneNumber", courtVenue.getPhoneNumber());
        assertEquals("postcode", courtVenue.getPostcode());
        assertEquals("welshSiteName", courtVenue.getWelshSiteName());
        assertEquals("siteName", courtVenue.getSiteName());
        assertEquals("3", courtVenue.getRegionId());
        assertEquals("venueName", courtVenue.getVenueName());
        assertEquals("Y", courtVenue.getIsCaseManagementLocation());
        assertEquals("N", courtVenue.getIsHearingLocation());
        assertEquals("serviceCode1", courtVenue.getServiceCode());
    }


    @Test
    void testCourtVenueBuilderNewFields() {

        CourtVenue courtVenue = CourtVenue.builder()
            .welshVenueName("testVenue")
            .isTemporaryLocation("N")
            .isNightingaleCourt("N")
            .locationType("Court")
            .parentLocation("366559")
            .welshCourtName("testWelshCourtName")
            .uprn("uprn123")
            .venueOuCode("venueOuCode1")
            .mrdBuildingLocationId("mrdBuildingLocationId1")
            .mrdVenueId("mrdVenueId1")
            .serviceUrl("serviceUrl1")
            .factUrl("factUrl1")
            .mrdCreatedTime("2020-01-01 00:00:00")
            .mrdUpdatedTime("2030-01-01 00:00:00")
            .mrdDeletedTime("2040-01-01 00:00:00")
            .externalShortName("External Court")
            .welshExternalShortName("Welsh External Court")
            .serviceCode("serviceCode1")
            .build();

        assertEquals("testVenue", courtVenue.getWelshVenueName());
        assertEquals("N", courtVenue.getIsTemporaryLocation());
        assertEquals("N", courtVenue.getIsNightingaleCourt());
        assertEquals("Court", courtVenue.getLocationType());
        assertEquals("366559", courtVenue.getParentLocation());
        assertEquals("testWelshCourtName", courtVenue.getWelshCourtName());
        assertEquals("uprn123", courtVenue.getUprn());
        assertEquals("venueOuCode1", courtVenue.getVenueOuCode());
        assertEquals("mrdBuildingLocationId1", courtVenue.getMrdBuildingLocationId());
        assertEquals("mrdVenueId1", courtVenue.getMrdVenueId());
        assertEquals("serviceUrl1", courtVenue.getServiceUrl());
        assertEquals("factUrl1", courtVenue.getFactUrl());
        assertEquals("2020-01-01 00:00:00", courtVenue.getMrdCreatedTime());
        assertEquals("2030-01-01 00:00:00", courtVenue.getMrdUpdatedTime());
        assertEquals("2040-01-01 00:00:00", courtVenue.getMrdDeletedTime());
        assertEquals("External Court", courtVenue.getExternalShortName());
        assertEquals("Welsh External Court", courtVenue.getWelshExternalShortName());
        assertEquals("serviceCode1", courtVenue.getServiceCode());
    }


    @Test
    void testCourtVenueBuilderCheck() {

        String courtVenueString = CourtVenue.builder()
            .courtName("courtName")
            .courtAddress("courtName")
            .welshCourtAddress("welshCourtAddress")
            .courtStatus("courtStatus")
            .courtTypeId("1")
            .dxAddress("dxAddress")
            .courtLocationCode("courtLocationCode")
            .courtOpenDate("courtOpenDate")
            .closedDate("closedDate")
            .clusterId("2")
            .epimmsId("epimmsId")
            .openForPublic("openForPublic")
            .phoneNumber("phoneNumber")
            .postcode("postcode")
            .welshSiteName("welshSiteName")
            .siteName("siteName")
            .regionId("3")
            .venueName("venueName")
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
            .mrdBuildingLocationId("mrdBuildingLocationId1")
            .mrdVenueId("mrdVenueId1")
            .serviceUrl("serviceUrl1")
            .factUrl("factUrl1")
            .externalShortName("shortName")
            .welshExternalShortName("welshExternalShortName")
            .serviceCode("serviceCode1")
            .toString();

        assertEquals("CourtVenue.CourtVenueBuilder(epimmsId=epimmsId, siteName=siteName, "
                         + "courtName=courtName, courtStatus=courtStatus, "
                         + "courtOpenDate=courtOpenDate, regionId=3, courtTypeId=1, "
                         + "clusterId=2, openForPublic=openForPublic, courtAddress=courtName, "
                         + "postcode=postcode, phoneNumber=phoneNumber, closedDate=closedDate, "
                         + "courtLocationCode=courtLocationCode, dxAddress=dxAddress, "
                         + "welshSiteName=welshSiteName, welshCourtAddress=welshCourtAddress, "
                         + "venueName=venueName, isCaseManagementLocation=Y, isHearingLocation=N, "
                         + "welshVenueName=testVenue, isTemporaryLocation=N, isNightingaleCourt=N, "
                         + "locationType=Court, parentLocation=366559, welshCourtName=testWelshCourtName, "
                         + "uprn=uprn123, venueOuCode=venueOuCode1, mrdBuildingLocationId=mrdBuildingLocationId1, "
                         + "mrdVenueId=mrdVenueId1, serviceUrl=serviceUrl1, factUrl=factUrl1, "
                         + "mrdCreatedTime=null, mrdUpdatedTime=null, mrdDeletedTime=null, "
                         + "externalShortName=shortName, "
                         + "welshExternalShortName=welshExternalShortName, "
                         + "serviceCode=serviceCode1)",
                     courtVenueString);

    }
}
