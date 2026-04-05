
create table fitbit_steps_ (
    user_id_     varchar(32) not null,
    date_        date not null,
    step_count_  integer null
);

alter table fitbit_steps_
    add constraint fitbit_steps_pk_ primary key (user_id_, date_);

create table fitbit_tcx_ (
    user_id_     varchar(32) not null,
    log_id_      bigint not null,
    tcx_xml_     varchar null
);

alter table fitbit_tcx_
    add constraint fitbit_tcx_pk_ primary key (user_id_, log_id_);
