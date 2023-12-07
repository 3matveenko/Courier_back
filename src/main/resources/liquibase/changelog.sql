-- liquibase formatted sql

-- changeset liquibase:1

create table t_drivers
(
    id                   bigint auto_increment
        primary key,
    latitude             double       null,
    longitude            double       null,
    name                 varchar(255) null,
    status_day           bit          null,
    status_order         bit          null,
    token                varchar(255) null,
    login                varchar(255) null,
    password             varchar(255) null,
    time_free            datetime(6)  null,
    last_update_location datetime(6)  null,
    time_free_today      datetime(6)  null,
    last_activity        datetime(6)  null
);

create table t_assigns
(
    id         bigint auto_increment
        primary key,
    time_start datetime(6) null,
    time_end   datetime(6) null,
    driver_id  bigint      null,
    constraint FKkm2twaaunlhlqwjctxpmwo685
        foreign key (driver_id) references t_drivers (id)
);

create table t_rejects
(
    id        bigint auto_increment
        primary key,
    comment   varchar(255) null,
    date      datetime(6)  null,
    latitude  double       null,
    longitude double       null,
    driver_id bigint       null,
    constraint FKhtinh02t33yj80v8nkc4wqa36
        foreign key (driver_id) references t_drivers (id)
);

create table t_orders
(
    id              bigint auto_increment
        primary key,
    latitude        double       null,
    longitude       double       null,
    addres          varchar(255) null,
    current         varchar(255) null,
    date_start      datetime(6)  null,
    date_end        datetime(6)  null,
    phone           varchar(255) null,
    guid            varchar(255) null,
    driver_id       bigint       null,
    status_delivery int          null,
    angle           double       null,
    reject_order_id bigint       null,
    comment         varchar(255) null,
    constraint FKe9c8pgbte4tnx7eej79pkyheu
        foreign key (driver_id) references t_drivers (id),
    constraint FKm531p19igu8w3syed184bkg6y
        foreign key (reject_order_id) references t_rejects (id)
);

create table t_assigns_orders
(
    assign_id bigint not null,
    orders_id bigint not null,
    constraint UK_9damfc25y233mrc3o5nidcg8c
        unique (orders_id),
    constraint FKajrwdhrqe7g5hsxftjpr2wsyx
        foreign key (assign_id) references t_assigns (id),
    constraint FKrrgbinrqf2wnbck410bev8o84
        foreign key (orders_id) references t_orders (id)
);

create table t_users
(
    id        bigint auto_increment
        primary key,
    email     varchar(255) null,
    user_name varchar(255) null,
    password  varchar(255) null
);

create table t_settings
(
    id       bigint auto_increment
        primary key,
    this_key varchar(255) null,
    value    varchar(255) null
);

INSERT INTO t_users (email, user_name, password)
SELECT 'admin@mail.ru', 'admin', '$2a$10$44984ect7TzyuxVpbouHuOPyyeYeoWdTpI6lWuiPd46cqEAaqC4Zu'
WHERE NOT EXISTS (SELECT 1 FROM t_users WHERE email = 'admin@mail.ru');

INSERT INTO t_settings (this_key, value)
SELECT 'crm_token', '108a5c76-552c-44cc-8af4-8050d0c886bc'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'crm_token');

INSERT INTO t_settings (this_key, value)
SELECT 'timer_sum', '0'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'timer_sum');

INSERT INTO t_settings (this_key, value)
SELECT 'timer_start_time', 'none'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'timer_start_time');

INSERT INTO t_settings (this_key, value)
SELECT 'angle', '90'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'angle');

INSERT INTO t_settings (this_key, value)
SELECT 'timer_sum_nodriver', '1'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = '1');

INSERT INTO t_settings (this_key, value)
SELECT 'fe_latitude', '43.24396869351982'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'fe_latitude');

INSERT INTO t_settings (this_key, value)
SELECT 'fe_longtitude', '76.89055824936942'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'fe_longtitude');

INSERT INTO t_settings (this_key, value)
SELECT 'protocol', 'http'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'protocol');

INSERT INTO t_settings (this_key, value)
SELECT 'server_name', '192.168.0.147'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'server_name');

INSERT INTO t_settings (this_key, value)
SELECT 'server_port', '8080'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'server_port');

INSERT INTO t_settings (this_key, value)
SELECT 'back_queue_name', 'back'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'back_queue_name');

INSERT INTO t_settings (this_key, value)
SELECT 'rabbit_server_name', '192.168.0.156'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'rabbit_server_name');

INSERT INTO t_settings (this_key, value)
SELECT 'rabbit_server_port', '5672'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'rabbit_server_port');

INSERT INTO t_settings (this_key, value)
SELECT 'rabbit_username', 'admin'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'rabbit_username');

INSERT INTO t_settings (this_key, value)
SELECT 'rabbit_password', '4217777'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'rabbit_password');

INSERT INTO t_settings (this_key, value)
SELECT 'order_distribution_principle', 'schedule'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'order_distribution_principle');

INSERT INTO t_settings (this_key, value)
SELECT 'beginning_work', '09:00'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'beginning_work');

INSERT INTO t_settings (this_key, value)
SELECT 'crm_server_address', 'http://192.168.0.136:80'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'crm_server_address');

-- changeset liquibase:2

INSERT INTO t_settings (this_key, value)
SELECT 'crm_login', 'admin'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'crm_login');

INSERT INTO t_settings (this_key, value)
SELECT 'crm_password', '4217777'
WHERE NOT EXISTS (SELECT 1 FROM t_settings WHERE this_key = 'crm_password');

ALTER TABLE t_drivers
DROP COLUMN time_free_today;

UPDATE t_users
SET password = '$2a$11$URHIpYejHnOW74mQ75TRju65Dl27Im.iWlzxLC8Qxg5ybmwVMBc4S',
    email = 'admin@admin.ru'
WHERE email = 'admin@mail.ru';

UPDATE t_settings
SET value = '28080'
WHERE this_key = 'server_port';

-- changeset liquibase:3

ALTER TABLE t_orders
ADD COLUMN sendSmS BOOLEAN;

-- changeset liquibase:4

ALTER TABLE t_assigns
    ADD COLUMN timeRun datetime(6) NULL;