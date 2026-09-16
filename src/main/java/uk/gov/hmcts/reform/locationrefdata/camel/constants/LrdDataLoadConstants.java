package uk.gov.hmcts.reform.locationrefdata.camel.constants;

public final class LrdDataLoadConstants {

    private LrdDataLoadConstants() {

    }

    public static final String ALPHANUMERIC_UNDERSCORE_REGEX = "[0-9a-zA-Z_]+";
    public static final String INVALID_EPIMS_ID = "Epims id is invalid - can contain only alphanumeric characters";
    public static final String IS_READY_TO_AUDIT = "IS_READY_TO_AUDIT";
    public static final String REGION_ID = "region_id";
    public static final String REGION_ID_NOT_EXISTS = "region_id does not exist";
    public static final String CLUSTER_ID = "cluster_id";
    public static final String CLUSTER_ID_NOT_EXISTS = "cluster_id does not exist";
    public static final String EPIMMS_ID = "epimms_id";
    public static final String EPIMMS_ID_NOT_EXISTS = "epimms_id does not exist";
    public static final String COURT_TYPE_ID = "court_type_id";
    public static final String COURT_TYPE_ID_NOT_EXISTS = "court_type_id does not exist";
    public static final String SERVICE_CODE = "service_code";
    public static final String SERVICE_CODE_NOT_EXISTS = "service_code does not exist";
    public static final String DATE_PATTERN = "\\d{2}-\\d{2}-\\d{4}\\s\\d{2}:\\d{2}:\\d{2}";
    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

}
