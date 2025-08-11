CREATE TABLE patient_profile (
                                 id int8 NOT NULL,
                                 first_name varchar(255) NULL,
                                 last_name varchar(255) NULL,
                                 status_id int2 NOT NULL,
                                 dob date NOT NULL,
                                 created_date_time timestamp NOT NULL,

                                 CONSTRAINT patient_profile_pkey PRIMARY KEY (id)

);

CREATE TABLE patient_old_client_guids (
                                          patient_id       int8 NOT NULL,
                                          old_client_guid  varchar(36) NOT NULL,

                                          CONSTRAINT patient_old_client_guids_pk
                                              PRIMARY KEY (patient_id, old_client_guid),
                                          CONSTRAINT fk_patient_old_client_guids_patient
                                              FOREIGN KEY (patient_id) REFERENCES patient_profile(id)
);

CREATE SEQUENCE patient_profile_id_seq
    START WITH 1
    INCREMENT BY 500;