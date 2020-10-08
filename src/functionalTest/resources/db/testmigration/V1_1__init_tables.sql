create schema if not exists locrefdata;
create table ORG_UNIT(
	org_unit_id bigint NOT NULL,
	description varchar(512),
	last_update timestamp,
	constraint org_unit_pk primary key (org_unit_id));


create table ORG_BUSINESS_AREA(
	business_area_id bigint NOT NULL,
	description varchar(512),
  last_update timestamp,
	constraint business_area_pk primary key (business_area_id)
);

create table ORG_SUB_BUSINESS_AREA(
	sub_business_area_id bigint NOT NULL,
	description varchar(512),
  last_update timestamp,
	constraint sub_business_area_id_pk primary key (sub_business_area_id)
);

create table JURISDICTION(
	jurisdiction_id bigint NOT NULL,
	description varchar(512),
  last_update timestamp,
	constraint jurisdiction_pk primary key (jurisdiction_id)
);

create table SERVICE(
	service_id bigint NOT NULL,
	org_unit_id bigint NOT NULL,
  business_area_id bigint NOT NULL,
	sub_business_area_id bigint NOT NULL,
	jurisdiction_id bigint NOT NULL,
	service_code varchar(16),
	service_description varchar(512),
	service_short_description varchar(256),
  last_update timestamp,
	constraint service_id_pk primary key (service_id),
	constraint service_code_uq1 unique (service_code)
);

create table SERVICE_TO_CCD_CASE_TYPE_ASSOC(
  id bigint NOT NULL,
	service_code varchar(16),
	ccd_service_name varchar(256),
	ccd_case_type varchar(256),
  created_date timestamp,
  constraint service_to_ccd_case_type_assoc_pk primary key (id),
	constraint service_to_ccd_case_type_assoc_unq unique (service_code, ccd_case_type)
);


ALTER TABLE SERVICE ADD CONSTRAINT org_unit_id FOREIGN KEY (org_unit_id)
REFERENCES ORG_UNIT (org_unit_id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE SERVICE ADD CONSTRAINT business_area_id FOREIGN KEY (business_area_id)
REFERENCES ORG_BUSINESS_AREA (business_area_id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE SERVICE ADD CONSTRAINT sub_business_area_id FOREIGN KEY (sub_business_area_id)
REFERENCES ORG_SUB_BUSINESS_AREA (sub_business_area_id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE SERVICE ADD CONSTRAINT jurisdiction_id FOREIGN KEY (jurisdiction_id)
REFERENCES JURISDICTION (jurisdiction_id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE SERVICE_TO_CCD_CASE_TYPE_ASSOC ADD CONSTRAINT service_code FOREIGN KEY (service_code)
REFERENCES SERVICE (service_code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
