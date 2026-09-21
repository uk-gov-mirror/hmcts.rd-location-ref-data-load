package uk.gov.hmcts.reform.locationrefdata.camel.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.hmcts.reform.locationrefdata.camel.binder.CourtVenue;
import uk.gov.hmcts.reform.locationrefdata.camel.service.ChildTableDataSyncService;
import uk.gov.hmcts.reform.locationrefdata.camel.service.ChildTableSyncDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.locationrefdata.camel.util.LrdLoadUtils.trim;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourtVenueChildTableSyncProcessor implements Processor {

    public static final String COURT_VENUES_EXCHANGE_PROPERTY = "courtVenueChildTableSyncCourtVenues";

    private static final String MRD_VENUE_ID = "mrd_venue_id";
    private static final String LANGUAGE_CODE = "language_code";
    private static final String ENGLISH = "EN";
    private static final String WELSH = "CY";
    private static final String CONTACT_METHOD_CODE = "contact_method_code";
    private static final String CONTACT_TYPE_CODE = "contact_type_code";
    private static final String CONTACT_VALUE = "contact_value";

    private static final ChildTableSyncDefinition REFERENCE_CODES = new ChildTableSyncDefinition(
        "reference_codes",
        List.of(MRD_VENUE_ID, "reference_code_type", "reference_code"),
        List.of()
    );
    private static final ChildTableSyncDefinition COURT_STATUS = new ChildTableSyncDefinition(
        "court_status",
        List.of("court_status_code"),
        List.of(LANGUAGE_CODE, "court_status_desc")
    );
    private static final ChildTableSyncDefinition COURT_VENUE_NAME = new ChildTableSyncDefinition(
        "court_venue_name",
        List.of(MRD_VENUE_ID, "court_name_type", LANGUAGE_CODE),
        List.of("name_desc")
    );
    private static final ChildTableSyncDefinition ADDRESS = new ChildTableSyncDefinition(
        "address",
        List.of(MRD_VENUE_ID, "address_type", LANGUAGE_CODE),
        List.of("address", "post_code", "uprn")
    );
    private static final ChildTableSyncDefinition CONTACT_DETAILS = new ChildTableSyncDefinition(
        "contact_details",
        List.of(MRD_VENUE_ID, "contact_method_code", "contact_type_code"),
        List.of("contact_value")
    );
    private static final ChildTableSyncDefinition CONTACT_METHOD = new ChildTableSyncDefinition(
        "contact_method",
        List.of("contact_method_code"),
        List.of(LANGUAGE_CODE, "contact_method_desc")
    );
    private static final ChildTableSyncDefinition COURT_USE_MAPPING = new ChildTableSyncDefinition(
        "court_use_mapping",
        List.of(MRD_VENUE_ID, "use_type_code"),
        List.of()
    );
    private static final ChildTableSyncDefinition COURT_VENUE_URL = new ChildTableSyncDefinition(
        "court_venue_url",
        List.of(MRD_VENUE_ID, "url_type"),
        List.of("url")
    );

    private final ChildTableDataSyncService childTableDataSyncService;
    private final JdbcTemplate jdbcTemplate;

    private CourtVenueFileDataStore courtVenueFileDataStore = new CourtVenueFileDataStore();

    @Override
    @Transactional
    public void process(Exchange exchange) {
        List<CourtVenue> courtVenues = getCourtVenues(exchange);
        if (courtVenues.isEmpty()) {
            log.info("Court venue child table sync skipped as no valid court venues are available");
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    syncChildTables(courtVenues);
                }
            });
            return;
        }

        syncChildTables(courtVenues);
    }

    private void syncChildTables(List<CourtVenue> courtVenues) {
        clearCourtVenueStatusCodes();
        childTableDataSyncService.sync(CONTACT_DETAILS, List.of());
        childTableDataSyncService.sync(COURT_STATUS, courtStatusRows(courtVenues));
        childTableDataSyncService.sync(CONTACT_METHOD, contactMethodRows(courtVenues));
        updateCourtVenueStatusCodes(courtVenues);
        childTableDataSyncService.sync(COURT_VENUE_NAME, courtVenueNameRows(courtVenues));
        childTableDataSyncService.sync(ADDRESS, addressRows(courtVenues));
        childTableDataSyncService.sync(CONTACT_DETAILS, contactDetailRows(courtVenues));
        childTableDataSyncService.sync(COURT_USE_MAPPING, courtUseMappingRows(courtVenues));
        childTableDataSyncService.sync(REFERENCE_CODES, referenceCodeRows(courtVenues));
        childTableDataSyncService.sync(COURT_VENUE_URL, courtVenueUrlRows(courtVenues));
        deleteCourtVenuesMissingFromFile(courtVenues);
    }

    private List<CourtVenue> getCourtVenues(Exchange exchange) {
        List<CourtVenue> storedCourtVenues = courtVenueFileDataStore.remove(courtVenueFileDataStore.jobKey(exchange));
        if (!storedCourtVenues.isEmpty()) {
            return validCourtVenues(storedCourtVenues);
        }

        Object courtVenues = exchange.getProperty(COURT_VENUES_EXCHANGE_PROPERTY);
        if (courtVenues instanceof List<?>) {
            List<?> values = (List<?>) courtVenues;
            List<CourtVenue> exchangeCourtVenues = values.stream()
                .filter(CourtVenue.class::isInstance)
                .map(CourtVenue.class::cast)
                .toList();
            return validCourtVenues(exchangeCourtVenues);
        }
        return List.of();
    }

    private List<CourtVenue> validCourtVenues(List<CourtVenue> courtVenues) {
        return courtVenues.stream()
            .filter(courtVenue -> StringUtils.isNotBlank(trim(courtVenue.getMrdVenueId())))
            .toList();
    }

    private List<Map<String, Object>> courtStatusRows(List<CourtVenue> courtVenues) {
        List<Map<String, Object>> rows = new ArrayList<>();
        courtVenues.forEach(courtVenue -> {
            String courtStatusCode = courtStatusCode(courtVenue.getCourtStatus());
            if (StringUtils.isNotBlank(courtStatusCode)) {
                rows.add(row(
                    "court_status_code", courtStatusCode,
                    LANGUAGE_CODE, ENGLISH,
                    "court_status_desc", courtStatusDescription(courtStatusCode)
                ));
            }
        });
        return rows;
    }

    private List<Map<String, Object>> contactMethodRows(List<CourtVenue> courtVenues) {
        List<Map<String, Object>> rows = new ArrayList<>();
        courtVenues.forEach(courtVenue -> {
            if (StringUtils.isNotBlank(trim(courtVenue.getPhoneNumber()))) {
                rows.add(row(
                    CONTACT_METHOD_CODE, "PHONE",
                    LANGUAGE_CODE, ENGLISH,
                    "contact_method_desc", "Phone"
                ));
            }
            if (StringUtils.isNotBlank(trim(courtVenue.getContactEmail()))
                || StringUtils.isNotBlank(trim(courtVenue.getBreathingSpaceEmail()))) {
                rows.add(row(
                    CONTACT_METHOD_CODE, "EMAIL",
                    LANGUAGE_CODE, ENGLISH,
                    "contact_method_desc", "Email"
                ));
            }
        });
        return rows;
    }

    private List<Map<String, Object>> courtVenueNameRows(List<CourtVenue> courtVenues) {
        List<Map<String, Object>> rows = new ArrayList<>();
        courtVenues.forEach(courtVenue -> {
            addCourtVenueName(rows, courtVenue, "SITE", ENGLISH, courtVenue.getSiteName());
            addCourtVenueName(rows, courtVenue, "COURT", ENGLISH, courtVenue.getCourtName());
            addCourtVenueName(rows, courtVenue, "VENUE", ENGLISH, courtVenue.getVenueName());
            addCourtVenueName(rows, courtVenue, "SITE", WELSH, courtVenue.getWelshSiteName());
            addCourtVenueName(rows, courtVenue, "COURT", WELSH, courtVenue.getWelshCourtName());
            addCourtVenueName(rows, courtVenue, "VENUE", WELSH, courtVenue.getWelshVenueName());
            addCourtVenueName(rows, courtVenue, "EXTERNAL_SHORT", ENGLISH, courtVenue.getExternalShortName());
            addCourtVenueName(rows, courtVenue, "EXTERNAL_SHORT", WELSH, courtVenue.getWelshExternalShortName());
            addCourtVenueName(
                rows,
                courtVenue,
                "DISTRICT_REGISTRY_SITE",
                ENGLISH,
                courtVenue.getDistrictRegistrySiteName()
            );
            addCourtVenueName(
                rows,
                courtVenue,
                "DISTRICT_REGISTRY_SITE",
                WELSH,
                courtVenue.getDistrictRegistryWelshSiteName()
            );
            addCourtVenueName(
                rows,
                courtVenue,
                "DISTRICT_REGISTRY_EXTERNAL_SHORT",
                ENGLISH,
                courtVenue.getDistrictRegistryExternalShortName()
            );
            addCourtVenueName(
                rows,
                courtVenue,
                "DISTRICT_REGISTRY_EXTERNAL_SHORT",
                WELSH,
                courtVenue.getDistrictRegistryWelshExternalShortName()
            );
        });
        return rows;
    }

    private List<Map<String, Object>> addressRows(List<CourtVenue> courtVenues) {
        List<Map<String, Object>> rows = new ArrayList<>();
        courtVenues.forEach(courtVenue -> {
            addAddress(rows, courtVenue, ENGLISH, courtVenue.getCourtAddress());
            addAddress(rows, courtVenue, WELSH, courtVenue.getWelshCourtAddress());
        });
        return rows;
    }

    private List<Map<String, Object>> contactDetailRows(List<CourtVenue> courtVenues) {
        List<Map<String, Object>> rows = new ArrayList<>();
        courtVenues.forEach(courtVenue -> {
            if (StringUtils.isNotBlank(trim(courtVenue.getPhoneNumber()))) {
                rows.add(row(
                    MRD_VENUE_ID, trim(courtVenue.getMrdVenueId()),
                    CONTACT_METHOD_CODE, "PHONE",
                    CONTACT_TYPE_CODE, "CONTACT_SERVICE",
                    CONTACT_VALUE, trim(courtVenue.getPhoneNumber())
                ));
            }
            if (StringUtils.isNotBlank(trim(courtVenue.getContactEmail()))) {
                rows.add(row(
                    MRD_VENUE_ID, trim(courtVenue.getMrdVenueId()),
                    CONTACT_METHOD_CODE, "EMAIL",
                    CONTACT_TYPE_CODE, "CONTACT_SERVICE",
                    CONTACT_VALUE, trim(courtVenue.getContactEmail())
                ));
            }
            if (StringUtils.isNotBlank(trim(courtVenue.getBreathingSpaceEmail()))) {
                rows.add(row(
                    MRD_VENUE_ID, trim(courtVenue.getMrdVenueId()),
                    CONTACT_METHOD_CODE, "EMAIL",
                    CONTACT_TYPE_CODE, "BREATHING_SPACE",
                    CONTACT_VALUE, trim(courtVenue.getBreathingSpaceEmail())
                ));
            }
        });
        return rows;
    }

    private List<Map<String, Object>> courtUseMappingRows(List<CourtVenue> courtVenues) {
        List<Map<String, Object>> rows = new ArrayList<>();
        courtVenues.forEach(courtVenue -> {
            addUseMapping(rows, courtVenue, "CASE_MANAGEMENT", courtVenue.getIsCaseManagementLocation());
            addUseMapping(rows, courtVenue, "HEARING", courtVenue.getIsHearingLocation());
            addUseMapping(rows, courtVenue, "TEMPORARY", courtVenue.getIsTemporaryLocation());
            addUseMapping(rows, courtVenue, "NIGHTINGALE", courtVenue.getIsNightingaleCourt());
            addUseMapping(rows, courtVenue, "DISTRICT_REGISTRY", courtVenue.getIsDistrictRegistry());
            addUseMapping(rows, courtVenue, "APPEAL_CENTRE", courtVenue.getIsAppealCentre());
        });
        return rows;
    }

    private List<Map<String, Object>> referenceCodeRows(List<CourtVenue> courtVenues) {
        List<Map<String, Object>> rows = new ArrayList<>();
        courtVenues.forEach(courtVenue -> {
            addReferenceCode(rows, courtVenue, "COURT_LOCATION_CODE", courtVenue.getCourtLocationCode());
            addReferenceCode(rows, courtVenue, "VENUE_OU_CODE", courtVenue.getVenueOuCode());
        });
        return rows;
    }

    private List<Map<String, Object>> courtVenueUrlRows(List<CourtVenue> courtVenues) {
        List<Map<String, Object>> rows = new ArrayList<>();
        courtVenues.forEach(courtVenue -> {
            addCourtVenueUrl(rows, courtVenue, "SERVICE", courtVenue.getServiceUrl());
            addCourtVenueUrl(rows, courtVenue, "FACT", courtVenue.getFactUrl());
        });
        return rows;
    }

    private void addCourtVenueName(List<Map<String, Object>> rows,
                                   CourtVenue courtVenue,
                                   String courtNameType,
                                   String languageCode,
                                   String nameDesc) {
        if (StringUtils.isNotBlank(trim(nameDesc))) {
            rows.add(row(
                MRD_VENUE_ID, trim(courtVenue.getMrdVenueId()),
                "court_name_type", courtNameType,
                LANGUAGE_CODE, languageCode,
                "name_desc", trim(nameDesc)
            ));
        }
    }

    private void addAddress(List<Map<String, Object>> rows,
                            CourtVenue courtVenue,
                            String languageCode,
                            String address) {
        if (StringUtils.isNotBlank(trim(address))) {
            rows.add(row(
                MRD_VENUE_ID, trim(courtVenue.getMrdVenueId()),
                "address_type", "MAILING",
                LANGUAGE_CODE, languageCode,
                "address", trim(address),
                "post_code", trim(courtVenue.getPostcode()),
                "uprn", trim(courtVenue.getUprn())
            ));
        }
    }

    private void addUseMapping(List<Map<String, Object>> rows,
                               CourtVenue courtVenue,
                               String useTypeCode,
                               String enabled) {
        if ("Y".equalsIgnoreCase(trim(enabled))) {
            rows.add(row(
                MRD_VENUE_ID, trim(courtVenue.getMrdVenueId()),
                "use_type_code", useTypeCode
            ));
        }
    }

    private void addReferenceCode(List<Map<String, Object>> rows,
                                  CourtVenue courtVenue,
                                  String referenceCodeType,
                                  String referenceCode) {
        if (StringUtils.isNotBlank(trim(referenceCode))) {
            rows.add(row(
                MRD_VENUE_ID, trim(courtVenue.getMrdVenueId()),
                "reference_code_type", referenceCodeType,
                "reference_code", trim(referenceCode)
            ));
        }
    }

    private void addCourtVenueUrl(List<Map<String, Object>> rows,
                                  CourtVenue courtVenue,
                                  String urlType,
                                  String url) {
        if (StringUtils.isNotBlank(trim(url))) {
            rows.add(row(
                MRD_VENUE_ID, trim(courtVenue.getMrdVenueId()),
                "url_type", urlType,
                "url", trim(url)
            ));
        }
    }

    private Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            row.put((String) values[index], values[index + 1]);
        }
        return row;
    }

    private String courtStatusCode(String courtStatus) {
        String trimmedCourtStatus = trim(courtStatus);
        if (StringUtils.isBlank(trimmedCourtStatus)) {
            return null;
        }
        return trimmedCourtStatus.toUpperCase().replace(' ', '_');
    }

    private String courtStatusDescription(String courtStatusCode) {
        return StringUtils.capitalize(courtStatusCode.toLowerCase().replace('_', ' '));
    }

    private void clearCourtVenueStatusCodes() {
        jdbcTemplate.update("UPDATE court_venue SET court_status_code = NULL WHERE court_status_code IS NOT NULL");
    }

    private void updateCourtVenueStatusCodes(List<CourtVenue> courtVenues) {
        List<Object[]> statusUpdates = courtVenues.stream()
            .filter(courtVenue -> StringUtils.isNotBlank(courtStatusCode(courtVenue.getCourtStatus())))
            .map(courtVenue -> new Object[] {
                courtStatusCode(courtVenue.getCourtStatus()),
                trim(courtVenue.getMrdVenueId())
            })
            .toList();

        if (!statusUpdates.isEmpty()) {
            jdbcTemplate.batchUpdate(
                "UPDATE court_venue SET court_status_code = ? WHERE mrd_venue_id = ?",
                statusUpdates
            );
        }
    }

    private void deleteCourtVenuesMissingFromFile(List<CourtVenue> courtVenues) {
        Set<String> desiredVenueIds = courtVenues.stream()
            .map(CourtVenue::getMrdVenueId)
            .map(this::trimVenueId)
            .filter(StringUtils::isNotBlank)
            .collect(HashSet::new, Set::add, Set::addAll);

        List<Object[]> venueIdsToDelete = jdbcTemplate.queryForList("SELECT mrd_venue_id FROM court_venue")
            .stream()
            .map(row -> trimVenueId((String) row.get(MRD_VENUE_ID)))
            .filter(StringUtils::isNotBlank)
            .filter(existingVenueId -> !desiredVenueIds.contains(existingVenueId))
            .map(existingVenueId -> new Object[] {existingVenueId})
            .toList();

        if (!venueIdsToDelete.isEmpty()) {
            deleteLegacyJurisdictionAssociations(venueIdsToDelete);
            clearSelfReferencesToDeletedCourtVenues(venueIdsToDelete);
            jdbcTemplate.batchUpdate("DELETE FROM court_venue WHERE mrd_venue_id = ?", venueIdsToDelete);
        }
    }

    private void deleteLegacyJurisdictionAssociations(List<Object[]> venueIdsToDelete) {
        jdbcTemplate.batchUpdate(
            "DELETE FROM court_district_family_jurisdiction_assoc "
                + "WHERE court_location_id = (SELECT court_venue_id FROM court_venue WHERE mrd_venue_id = ?)",
            venueIdsToDelete
        );
        jdbcTemplate.batchUpdate(
            "DELETE FROM court_district_civil_jurisdiction_assoc "
                + "WHERE court_location_id = (SELECT court_venue_id FROM court_venue WHERE mrd_venue_id = ?)",
            venueIdsToDelete
        );
    }

    private void clearSelfReferencesToDeletedCourtVenues(List<Object[]> venueIdsToDelete) {
        jdbcTemplate.batchUpdate("UPDATE court_venue SET parent_id = NULL WHERE parent_id = ?", venueIdsToDelete);
        jdbcTemplate.batchUpdate(
            "UPDATE court_venue SET district_registry_venue_id = NULL WHERE district_registry_venue_id = ?",
            venueIdsToDelete
        );
        jdbcTemplate.batchUpdate(
            "UPDATE court_venue SET appeal_centre_venue_id = NULL WHERE appeal_centre_venue_id = ?",
            venueIdsToDelete
        );
    }

    private String trimVenueId(String venueId) {
        return trim(venueId);
    }
}
