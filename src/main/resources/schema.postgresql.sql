
-- Profile

create table fitbit_profile_ (
    user_id_                    varchar(32) not null,
    display_name_               varchar null,
    full_name_                  varchar null,
    avatar_url_                 varchar null,
    date_of_birth_              varchar null,
    age_                        integer null,
    gender_                     varchar null,
    member_since_               varchar null,
    height_                     integer null,
    weight_                     integer null,
    stride_length_walking_      double precision null,
    stride_length_running_      double precision null,
    average_daily_steps_        varchar null
);

alter table fitbit_profile_
    add constraint fitbit_profile_pk_ primary key (user_id_);

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
    log_id_                 bigint not null,
    log_type_               varchar(32) null,
    activity_type_id_       integer null,
    activity_name_          varchar(64) null,
    date_                   date null,
    beg_date_time_          timestamptz null,
    end_date_time_          timestamptz null,
    duration_minutes_       double precision null,
    duration_active_minutes_ double precision null,
    distance_               double precision null,
    steps_                  integer null,
    calories_               integer null,
    speed_                  double precision null,
    pace_                   double precision null,
    average_heart_rate_     integer null,
    active_zone_minutes_    integer null,
    source_type_            varchar null,
    source_name_            varchar null,
    source_url_             varchar null,
    source_features_        varchar null
);

alter table fitbit_activity_
    add constraint fitbit_activity_pk_ primary key (user_id_, log_id_);

create table fitbit_activity_level_ (
    user_id_                varchar(32) not null,
    log_id_                 bigint not null,
    level_name_             varchar(32) not null,
    level_minutes_          integer null,
    sort_index_             integer null
);

alter table fitbit_activity_level_
    add constraint fitbit_activity_level_pk_ primary key (user_id_, log_id_, level_name_);

alter table fitbit_activity_level_
    add constraint fitbit_activity_level_activity_fk_ foreign key (user_id_, log_id_) references fitbit_activity_ (user_id_, log_id_);

create table fitbit_activity_heartrate_zone_ (
    user_id_                varchar(32) not null,
    log_id_                 bigint not null,
    zone_name_              varchar(32) not null,
    zone_minutes_           integer null,
    calories_               double precision null,
    heartrate_min_          integer null,
    heartrate_max_          integer null
);

alter table fitbit_activity_heartrate_zone_
    add constraint fitbit_activity_heartrate_zone_pk_ primary key (user_id_, log_id_, zone_name_);

alter table fitbit_activity_heartrate_zone_
    add constraint fitbit_activity_heartrate_zone_activity_fk_ foreign key (user_id_, log_id_) references fitbit_activity_ (user_id_, log_id_);

-- Activities - TCX

create table fitbit_tcx_ (
    user_id_                varchar(32) not null,
    log_id_                 bigint not null,
    tcx_xml_                varchar null,
    date_                   date null,
    beg_date_time_          timestamptz null,
    end_date_time_          timestamptz null,
    duration_minutes_       double precision null,
    distance_               double precision null,
    latitude_min_           double precision null,
    latitude_max_           double precision null,
    longitude_min_          double precision null,
    longitude_max_          double precision null,
    altitude_min_           double precision null,
    altitude_max_           double precision null,
    heart_rate_max_         integer null
);

alter table fitbit_tcx_
    add constraint fitbit_tcx_pk_ primary key (user_id_, log_id_);
