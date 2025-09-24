create table avatar
(
    id            int auto_increment
        primary key,
    name          char(100) null,
    description   text      null,
    category      char(100) null,
    system_prompt text      null
);

create table model
(
    id               int auto_increment
        primary key,
    model_name       varchar(255)                       null,
    base_url         varchar(255)                       null,
    api_key          varchar(255)                       null,
    completions_path varchar(255)                       null,
    model_version    varchar(255)                       null,
    create_time      datetime default (now())           null,
    update_time      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table user
(
    id          int auto_increment comment 'id'
        primary key,
    name        int                                null,
    uuid        char(14)                           null,
    sex         int      default 0                 null comment '1-男生 ,2-女生,0-未设置',
    old         int                                null comment '年龄',
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table user_avatar
(
    id        int auto_increment
        primary key,
    user_id   int null,
    avatar_id int null
);

