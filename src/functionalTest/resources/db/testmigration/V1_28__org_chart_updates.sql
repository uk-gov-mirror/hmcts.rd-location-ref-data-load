update locrefdata.jurisdiction set description = 'Employment Tribunals', last_update = timezone('utc', now())
where jurisdiction_id = (select jurisdiction_id from locrefdata.jurisdiction where description = 'Employment Tribunerals');

insert into jurisdiction (description, last_update)
values
    ('Upper Tribunal Tax and Chancery Chamber', timezone('utc', now())),
    ('Upper Tribunal Lands Chamber', timezone('utc', now())),
    ('Upper Tribunal Administrative Appeals Chamber', timezone('utc', now())),
    ('Upper Tribunal Immigration and Asylum Chamber', timezone('utc', now())),
    ('Employment Appeals', timezone('utc', now()));

delete from locrefdata.service where service_code = 'BAA1' and service_description = 'Alternative Business Structures';

delete from locrefdata.service where service_code = 'BAA3' and service_description = 'Claims Management Services';

delete from locrefdata.service where service_code = 'BAA4' and service_description = 'Consumer Credit';

update locrefdata.service set service_description = 'Gambling', last_update = timezone('utc', now())
where service_code = 'BAA8' and service_description = 'Gambling Appeals';

delete from locrefdata.service where service_code = 'BAB2' and service_description = 'Local Government Standards';

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  46,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='General Regulatory Chamber'),
  'BAB4',
  'Community Right To Bid',
  'Community Right To Bid',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  47,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='General Regulatory Chamber'),
  'BAB5',
  'Electronic Communications, Postal Services & Network Information Systems',
  'Electronic Communications, Postal Services & Network Information Systems',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  48,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='General Regulatory Chamber'),
  'BAB6',
  'Food Safety',
  'Food Safety',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  49,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='General Regulatory Chamber'),
  'BAB7',
  'Individual Electoral Registration',
  'Individual Electoral Registration',
  timezone('utc', now()));


insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  50,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='General Regulatory Chamber'),
  'BAB8',
  'Licensing and Standards',
  'Licensing and Standards',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  51,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='General Regulatory Chamber'),
  'BAB9',
  'Pensions',
  'Pensions',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  52,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='General Regulatory Chamber'),
  'BAC1',
  'Welfare of Animals',
  'Welfare of Animals',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  53,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Tax and Chancery Chamber'),
  'BTA1',
  'Upper Tribunals Tax Appeals & MP’s Expenses',
  'Upper Tribunals Tax Appeals & MP’s Expenses',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  54,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Tax and Chancery Chamber'),
  'BTA2',
  'Upper Tribunals Charity',
  'Upper Tribunals Charity',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  55,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Tax and Chancery Chamber'),
  'BTA3',
  'Upper Tribunals Notice of Reference Finance Services',
  'Upper Tribunals Notice of Reference Finance Services',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  56,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Tax and Chancery Chamber'),
  'BTA4',
  'Upper Tribunals Notice of Reference Energy Market Decisions',
  'Upper Tribunals Notice of Reference Energy Market Decisions',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  57,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Tax and Chancery Chamber'),
  'BTA5',
  'Upper Tribunals Notice of Reference Trade Remedies',
  'Upper Tribunals Notice of Reference Trade Remedies',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  58,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Tax and Chancery Chamber'),
  'BTA6',
  'Upper Tribunals Notice of Appeal Trade Remedies',
  'Upper Tribunals Notice of Appeal Trade Remedies',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  59,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Tax and Chancery Chamber'),
  'BTA7',
  'Upper Tribunals Notice of Appeal Financial Sanctions',
  'Upper Tribunals Notice of Appeal Financial Sanctions',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  60,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Tax and Chancery Chamber'),
  'BTA8',
  'Upper Tribunals Judicial Review Applications (Tax)',
  'Upper Tribunals Judicial Review Applications (Tax)',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  61,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLA1',
  'Upper Tribunals Agricultural Land and Drainage',
  'Upper Tribunals Agricultural Land and Drainage',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  62,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLA2',
  'Upper Tribunals Land Registration',
  'Upper Tribunals Land Registration',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  63,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLA3',
  'Upper Tribunals First-Tier (Property Chamber)',
  'Upper Tribunals First-Tier (Property Chamber)',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  64,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLA4',
  'Upper Tribunals Leasehold Valuation Tribunal in Wales',
  'Upper Tribunals Leasehold Valuation Tribunal in Wales',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  65,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLA5',
  'Upper Tribunals Residential Property in Wales',
  'Upper Tribunals Residential Property in Wales',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  66,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLA6',
  'Upper Tribunals Law Property Act 1925 Applications',
  'Upper Tribunals Law Property Act 1925 Applications',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  67,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLA7',
  'Upper Tribunals Ratings Appeals',
  'Upper Tribunals Ratings Appeals',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  68,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLA8',
  'Upper Tribunals Notice of Reference',
  'Upper Tribunals Notice of Reference',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  69,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLA9',
  'Upper Tribunals Rights of Light Applications',
  'Upper Tribunals Rights of Light Applications',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  70,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLB1',
  'Upper Tribunals Absent Owner Applications',
  'Upper Tribunals Absent Owner Applications',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  71,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Lands Chamber'),
  'BLB2',
  'Upper Tribunals Applications under s.33 Landlords and Tenant act 1987',
  'Upper Tribunals Applications under s.33 Landlords and Tenant act 1987',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  72,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Immigration and Asylum Chamber'),
  'BIA1',
  'Upper Tribunals Immigration and Asylum Appeals',
  'Upper Tribunals Immigration and Asylum Appeals',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  73,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKA4',
  'Upper Tribunals Environment',
  'Upper Tribunals Environment',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  74,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKA5',
  'Upper Tribunals Estate Agents',
  'Upper Tribunals Estate Agents',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  75,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKA6',
  'Upper Tribunals Examination Boards',
  'Upper Tribunals Examination Boards',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  76,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKA7',
  'Upper Tribunals Gambling Appeals',
  'Upper Tribunals Gambling Appeals',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  77,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKA8',
  'Upper Tribunals Immigration Services',
  'Upper Tribunals Immigration Services',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  78,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKA9',
  'Upper Tribunals Information Rights',
  'Upper Tribunals Information Rights',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  79,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKB1',
  'Upper Tribunals Driving Instructor',
  'Upper Tribunals Driving Instructor',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  80,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKB2',
  'Upper Tribunals Criminal Injuries Compensation',
  'Upper Tribunals Criminal Injuries Compensation',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  81,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKB3',
  'Upper Tribunals Social Security and Child Support',
  'Upper Tribunals Social Security and Child Support',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  82,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKB4',
  'Upper Tribunals Care Standards',
  'Upper Tribunals Care Standards',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  83,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKB5',
  'Upper Tribunals Mental Health',
  'Upper Tribunals Mental Health',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  84,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKB6',
  'Upper Tribunals Primary Health Lists',
  'Upper Tribunals Primary Health Lists',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  85,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKB7',
  'Upper Tribunals Special Educational Needs and Disability',
  'Upper Tribunals Special Educational Needs and Disability',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  86,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKB8',
  'Upper Tribunals War Pensions Appeals',
  'Upper Tribunals War Pensions Appeals',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  87,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKB9',
  'Upper Tribunals Safeguarding decisions of Disclosure Barring Service',
  'Upper Tribunals Safeguarding decisions of Disclosure Barring Service',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  88,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKC1',
  'Upper Tribunals Transport from the decisions of the traffic commissioners',
  'Upper Tribunals Transport from the decisions of the traffic commissioners',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  89,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKC2',
  'Upper Tribunals Food Safety',
  'Upper Tribunals Food Safety',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  90,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKC3',
  'Upper Tribunals Professional Regulation',
  'Upper Tribunals Professional Regulation',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  91,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKC4',
  'Upper Tribunals Welfare of Animals',
  'Upper Tribunals Welfare of Animals',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  92,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKC5',
  'Upper Tribunals Pensions Regulation',
  'Upper Tribunals Pensions Regulation',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  93,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Upper Tribunal Administrative Appeals Chamber'),
  'BKC6',
  'Upper Tribunals Judicial Review Applications (AAC)',
  'Upper Tribunals Judicial Review Applications (AAC)',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  94,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Employment Appeals'),
  'BMA1',
  'Employment Appeals Employment Claims',
  'Employment Appeals Employment Claims',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  95,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Employment Appeals'),
  'BMA2',
  'Employment Appeals Central Arbitration Committee',
  'Employment Appeals Central Arbitration Committee',
  timezone('utc', now()));

insert into locrefdata.service (service_id, org_unit_id, business_area_id, sub_business_area_id, jurisdiction_id, service_code,
  service_description, service_short_description, last_update) values (
  96,
  (select org_unit_id from locrefdata.org_unit where description='HMCTS'),
  (select business_area_id from locrefdata.org_business_area where description ='Civil, Family and Tribunals'),
  (select sub_business_area_id from locrefdata.org_sub_business_area   where description ='Tribunals'),
  (select jurisdiction_id from locrefdata.jurisdiction where description ='Employment Appeals'),
  'BMA3',
  'Employment Appeals Certification Officer',
  'Employment Appeals Certification Officer',
  timezone('utc', now()));
