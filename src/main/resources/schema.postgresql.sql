
create table fitbit_steps_ (
    user_id_     varchar(32) not null,
    date_        date not null,
    step_count_  integer null
);

alter table fitbit_steps_
    add constraint fitbit_steps_pk_ primary key (user_id_, date_);
