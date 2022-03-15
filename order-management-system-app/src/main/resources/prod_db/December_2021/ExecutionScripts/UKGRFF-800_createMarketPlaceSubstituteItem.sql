CREATE TABLE OMSCOREDB.OMSCORE.MARKETPLACE_SUBSTITUTED_ITEM (
                                                                RECORD_ID uniqueidentifier NOT NULL,
                                                                DB_LOCK_VERSION bigint NULL,
                                                                CREATED_DATE datetime2 NULL,
                                                                MODIFIED_DATE datetime2 NULL,
                                                                ITEM_DESCRIPTION varchar(225) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
                                                                EXTERNAL_ITEM_ID varchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
                                                                ITEM_QUANTITY bigint NULL,
                                                                UNIT_PRICE float NULL,
                                                                TOTAL_PRICE float NULL,
                                                                ITEM_RECORD_ID uniqueidentifier NOT NULL,
                                                                CONSTRAINT MARKETPLACE_SUBSTITUTED_ITEM_PK PRIMARY KEY (RECORD_ID),
                                                                CONSTRAINT MARKETPLACE_SUBSTITUTED_ITEM_FK FOREIGN KEY (ITEM_RECORD_ID) REFERENCES OMSCOREDB.OMSCORE.MARKETPLACE_ITEM(RECORD_ID)
);