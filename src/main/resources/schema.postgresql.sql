
-- Steps

create table fitbit_steps_ (
    user_id_                varchar(32) not null,
    date_                   date not null,
    step_count_             integer null
);

alter table fitbit_steps_
    add constraint fitbit_steps_pk_ primary key (user_id_, date_);

-- Heart Rate

create table fitbit_heartrate_ (
    user_id_                varchar(32) not null,
    date_                   date not null,
    resting_heart_rate_     integer null
);

alter table fitbit_heartrate_
    add constraint fitbit_heartrate_pk_ primary key (user_id_, date_);

-- Cardio Score

create table fitbit_cardioscore_ (
    user_id_                varchar(32) not null,
    date_                   date not null,
    score_min_              integer null,
    score_max_              integer null
);

alter table fitbit_cardioscore_
    add constraint fitbit_cardioscore_pk_ primary key (user_id_, date_);

-- Active Zone Minutes

create table fitbit_active_zone_minutes_ (
    user_id_                varchar(32) not null,
    date_                   date not null,
    total_azm_              integer null,
    fat_burn_azm_           integer null,
    cardio_azm_             integer null,
    peak_azm_               integer null
);

alter table fitbit_active_zone_minutes_
    add constraint fitbit_active_zone_minutes_pk_ primary key (user_id_, date_);

-- Activities

create table fitbit_activity_ (
    user_id_                varchar(32) not null,
    date_                   date not null,
    log_id_                 bigint not null,
    beg_date_time_          timestamptz null,
    end_date_time_          timestamptz null,
    duration_minutes_       integer null,
    calories_               integer null,
    steps_                  integer null,
    log_type_               varchar(32) null,
    activity_name_          varchar(64) null,
    distance_               double precision null,
    distance_unit_          varchar(16) null,
    average_heart_rate_     integer null
);

alter table fitbit_activity_
    add constraint fitbit_activity_pk_ primary key (user_id_, date_, log_id_);

-- Activities - TCX

create table fitbit_tcx_ (
    user_id_                varchar(32) not null,
    log_id_                 bigint not null,
    tcx_xml_                varchar null
);

alter table fitbit_tcx_
    add constraint fitbit_tcx_pk_ primary key (user_id_, log_id_);
