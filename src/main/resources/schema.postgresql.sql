
-- Steps

create table fitbit_steps_ (
    user_id_     varchar(32) not null,
    date_        date not null,
    step_count_  integer null
);

alter table fitbit_steps_
    add constraint fitbit_steps_pk_ primary key (user_id_, date_);

-- Heart Rate

create table fitbit_heartrate_ (
    user_id_              varchar(32) not null,
    date_                 date not null,
    resting_heart_rate_   integer null
);

alter table fitbit_heartrate_
    add constraint fitbit_heartrate_pk_ primary key (user_id_, date_);

-- Cardio Score

create table fitbit_cardioscore_ (
    user_id_     varchar(32) not null,
    date_        date not null,
    score_min_   integer null,
    score_max_   integer null
);

alter table fitbit_cardioscore_
    add constraint fitbit_cardioscore_pk_ primary key (user_id_, date_);

-- Active Zone Minutes

create table fitbit_azm_ (
    user_id_                       varchar(32) not null,
    date_                          date not null,
    active_zone_minutes_           integer null,
    fat_burn_active_zone_minutes_  integer null,
    cardio_active_zone_minutes_    integer null,
    peak_active_zone_minutes_      integer null
);

alter table fitbit_azm_
    add constraint fitbit_azm_pk_ primary key (user_id_, date_);

-- Activities - TCX

create table fitbit_tcx_ (
    user_id_     varchar(32) not null,
    log_id_      bigint not null,
    tcx_xml_     varchar null
);

alter table fitbit_tcx_
    add constraint fitbit_tcx_pk_ primary key (user_id_, log_id_);
