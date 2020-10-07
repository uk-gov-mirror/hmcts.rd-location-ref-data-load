insert into service (
    org_unit_id,
    business_area_id,
    sub_business_area_id,
    jurisdiction_id,
    service_code,
    service_description,
    service_short_description,
    last_update)
    select (select org_unit_id from ORG_UNIT where description ='HMCTS') as org_unit_id,
    (select business_area_id from ORG_BUSINESS_AREA
     where description ='Civil, Family and Tribunals')
    as business_area_id,
    (select sub_business_area_id from ORG_SUB_BUSINESS_AREA
    where description ='Civil and Family')
    as sub_business_area_id,
    (select jurisdiction_id from JURISDICTION where description ='Civil')
    as jurisdiction_id,
    'AAA1' as service_code,
    'Civil Enforcement',
    'Civil Enforcement',
    now()
    from ORG_UNIT;

    insert into service (
    org_unit_id,
    business_area_id,
    sub_business_area_id,
    jurisdiction_id,
    service_code,
    service_description,
    service_short_description,
    last_update)
    select (select org_unit_id from ORG_UNIT where description ='HMCTS') as org_unit_id,
    (select business_area_id from ORG_BUSINESS_AREA
     where description ='Civil, Family and Tribunals')
    as business_area_id,
    (select sub_business_area_id from ORG_SUB_BUSINESS_AREA
    where description ='Civil and Family')
    as sub_business_area_id,
    (select jurisdiction_id from JURISDICTION where description ='Civil')
    as jurisdiction_id,
    'AAA2' as service_code,
    'Insolvency',
    'Insolvency',
    now()
    from ORG_UNIT;

     insert into service (
    org_unit_id,
    business_area_id,
    sub_business_area_id,
    jurisdiction_id,
    service_code,
    service_description,
    service_short_description,
    last_update)
    select (select org_unit_id from ORG_UNIT where description ='HMCTS') as org_unit_id,
    (select business_area_id from ORG_BUSINESS_AREA
     where description ='Civil, Family and Tribunals')
    as business_area_id,
    (select sub_business_area_id from ORG_SUB_BUSINESS_AREA
    where description ='Civil and Family')
    as sub_business_area_id,
    (select jurisdiction_id from JURISDICTION where description ='Civil')
    as jurisdiction_id,
    'AAA3' as service_code,
    'Insolvency3',
    'Insolvency3',
    now()
    from ORG_UNIT;

       insert into service (
    org_unit_id,
    business_area_id,
    sub_business_area_id,
    jurisdiction_id,
    service_code,
    service_description,
    service_short_description,
    last_update)
    select (select org_unit_id from ORG_UNIT where description ='HMCTS') as org_unit_id,
    (select business_area_id from ORG_BUSINESS_AREA
     where description ='Civil, Family and Tribunals')
    as business_area_id,
    (select sub_business_area_id from ORG_SUB_BUSINESS_AREA
    where description ='Civil and Family')
    as sub_business_area_id,
    (select jurisdiction_id from JURISDICTION where description ='Civil')
    as jurisdiction_id,
    'AAA4' as service_code,
    'Insolvency4',
    'Insolvency4',
    now()
    from ORG_UNIT;

