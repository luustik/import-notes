CREATE TABLE patient_note (
                              id int8 NOT NULL,
                              created_date_time timestamp NOT NULL,
                              last_modified_date_time timestamp NOT NULL,
                              created_by_user_id int8 NULL,
                              last_modified_by_user_id int8 NULL,
                              note varchar(4000) NULL,
                              patient_id int8 NOT NULL,
                              old_note_guid varchar(36) NOT NULL UNIQUE,

                              CONSTRAINT patient_note_pkey PRIMARY KEY (id),
                              CONSTRAINT fk_pat_note_modified_user
                                  FOREIGN KEY (last_modified_by_user_id) REFERENCES company_user(id),
                              CONSTRAINT fk_pat_note_created_user
                                  FOREIGN KEY (created_by_user_id) REFERENCES company_user(id),
                              CONSTRAINT fk_pat_note_patient
                                  FOREIGN KEY (patient_id) REFERENCES patient_profile(id)
);

CREATE SEQUENCE patient_note_id_seq
    START WITH 1
    INCREMENT BY 500;