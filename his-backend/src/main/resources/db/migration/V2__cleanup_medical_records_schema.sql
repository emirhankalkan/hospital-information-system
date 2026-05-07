-- medical_records no longer stores patient_id and doctor_id directly.
-- Patient and doctor data is reached through appointment_id.
alter table if exists medical_records
    drop constraint if exists fktny13k9v4o58styd47st3s2l5;

alter table if exists medical_records
    drop constraint if exists fkrav12h9aiw7pegjt62p8owwh3;

alter table if exists medical_records
    drop column if exists doctor_id;

alter table if exists medical_records
    drop column if exists patient_id;
