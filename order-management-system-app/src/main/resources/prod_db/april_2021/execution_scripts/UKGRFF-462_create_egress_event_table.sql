create table OMSCORE.EGRESS_EVENTS
(
    RECORD_ID         uniqueidentifier not null
        constraint EGRESS_EVENTS_PK
            primary key,
    CREATED_DATE      datetime2 null,
    MODIFIED_DATE     datetime2 null,
    DB_LOCK_VERSION   bigint null,
    SOURCE_DOMAIN     varchar(40)      not null,
    EVENT_NAME        varchar(40)      not null,
    EVENT_DESCRIPTION varchar(1000) null,
    MESSAGE           varchar(max
) not null,
    MESSAGE_FORMAT    varchar(40) null,
    EVENT_STATUS      varchar(40) null,
    RETRIES           int,
    JUST_AUDIT        bit,
    DOMAIN_MODEL_ID   varchar(40) not null
)
go