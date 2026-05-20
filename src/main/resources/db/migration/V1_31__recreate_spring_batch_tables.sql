-- ensure we have table names available
drop table if exists batch_step_execution_context_archived2;
drop table if exists batch_job_execution_context_archived2;
drop table if exists batch_step_execution_archived2;
drop table if exists batch_job_execution_params_archived2;
drop table if exists batch_job_execution_archived2;
drop table if exists batch_job_instance_archived2;

-- rename existing tables in environments (AAT has had successful job runs)
alter table if exists batch_step_execution_context rename to batch_step_execution_context_archived2;
alter table if exists batch_job_execution_context rename to batch_job_execution_context_archived2;
alter table if exists batch_step_execution rename to batch_step_execution_archived2;
alter table if exists batch_job_execution_params rename to batch_job_execution_params_archived2;
alter table if exists batch_job_execution rename to batch_job_execution_archived2;
alter table if exists batch_job_instance rename to batch_job_instance_archived2;

alter table if exists batch_job_instance_archived2 rename constraint JOB_INST_UN to JOB_INST_UN_A2;
alter table if exists batch_job_execution_archived2 rename constraint JOB_INST_EXEC_FK to JOB_INST_EXEC_FK_A2;
alter table if exists batch_job_execution_params_archived2 rename constraint JOB_EXEC_PARAMS_FK to JOB_EXEC_PARAMS_FK_A2;
alter table if exists batch_step_execution_archived2 rename constraint JOB_EXEC_STEP_FK to JOB_EXEC_STEP_FK_A2;
alter table if exists batch_step_execution_context_archived2 rename constraint STEP_EXEC_CTX_FK to STEP_EXEC_CTX_FK_A2;
alter table if exists batch_job_execution_context_archived2 rename constraint JOB_EXEC_CTX_FK to JOB_EXEC_CTX_FK_A2;

-- drop sequences
drop sequence if exists BATCH_STEP_EXECUTION_SEQ;
drop sequence if exists BATCH_JOB_EXECUTION_SEQ;
drop sequence if exists BATCH_JOB_SEQ;
