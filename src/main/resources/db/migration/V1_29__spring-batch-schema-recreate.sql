drop table if exists batch_step_execution_context_archived;
drop table if exists batch_job_execution_context_archived;
drop table if exists batch_step_execution_archived;
drop table if exists batch_job_execution_params_archived;
drop table if exists batch_job_execution_archived;
drop table if exists batch_job_instance_archived;

alter table if exists batch_step_execution_context rename to batch_step_execution_context_archived;
alter table if exists batch_job_execution_context rename to batch_job_execution_context_archived;
alter table if exists batch_step_execution rename to batch_step_execution_archived;
alter table if exists batch_job_execution_params rename to batch_job_execution_params_archived;
alter table if exists batch_job_execution rename to batch_job_execution_archived;
alter table if exists batch_job_instance rename to batch_job_instance_archived;

alter table if exists batch_job_instance_archived rename constraint JOB_INST_UN to JOB_INST_UN_A;
alter table if exists batch_job_execution_archived rename constraint JOB_INST_EXEC_FK to JOB_INST_EXEC_FK_A;
alter table if exists batch_job_execution_params_archived rename constraint JOB_EXEC_PARAMS_FK to JOB_EXEC_PARAMS_FK_A;
alter table if exists batch_step_execution_archived rename constraint JOB_EXEC_STEP_FK to JOB_EXEC_STEP_FK_A;
alter table if exists batch_step_execution_context_archived rename constraint STEP_EXEC_CTX_FK to STEP_EXEC_CTX_FK_A;
alter table if exists batch_job_execution_context_archived rename constraint JOB_EXEC_CTX_FK to JOB_EXEC_CTX_FK_A;

drop sequence if exists BATCH_STEP_EXECUTION_SEQ;
drop sequence if exists BATCH_JOB_EXECUTION_SEQ;
drop sequence if exists BATCH_JOB_SEQ;
