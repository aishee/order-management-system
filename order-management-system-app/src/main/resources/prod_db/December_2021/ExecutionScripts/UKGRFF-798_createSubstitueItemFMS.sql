CREATE TABLE OMSCOREDB.OMSCORE.FULLFILLMENT_ORDER_SUBSTITUTED_ITEM (
                                                                     RECORD_ID uniqueidentifier NOT NULL,
                                                                     DB_LOCK_VERSION bigint NULL,
                                                                     CREATED_DATE datetime2 NULL,
                                                                     MODIFIED_DATE datetime2 NULL,
                                                                     ITEM_DESCRIPTION varchar(225) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
                                                                     WALMART_ITEM_NUM varchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
                                                                     CONSUMER_ITEM_NUM varchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
                                                                     DEPARTMENT_ID varchar(8) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
                                                                     QUANTITY bigint NULL,
                                                                     WEIGHT float NULL,
                                                                     STORE_UNIT_PRICE float NULL,
                                                                     STORE_TOTAL_PRICE float NULL,
                                                                     PICKED_ITEM_RECORD_ID uniqueidentifier NOT NULL,
                                                                     CONSTRAINT FULLFILLMENT_ORDER_SUBSTITUTED_ITEM_PK PRIMARY KEY (RECORD_ID),
                                                                     CONSTRAINT FULLFILLMENT_ORDER_SUBSTITUTED_ITEM_FK FOREIGN KEY (PICKED_ITEM_RECORD_ID) REFERENCES OMSCOREDB.OMSCORE.FULFILLMENT_ORDER_PICKED_LINE(RECORD_ID)
);

CREATE TABLE OMSCOREDB.OMSCORE.FULFILLMENT_ORDER_SUBSTITUTED_ITEM_UPC (
                                                                        RECORD_ID uniqueidentifier NOT NULL,
                                                                        CREATED_DATE datetime2 NULL,
                                                                        MODIFIED_DATE datetime2 NULL,
                                                                        DB_LOCK_VERSION bigint NULL,
                                                                        UPC varchar(30) NOT NULL,
                                                                        UOM varchar(30) NULL,
                                                                        SUBSTITUTED_ITEM_RECORD_ID uniqueidentifier NOT NULL,
                                                                        CONSTRAINT FULFILLMENT_ORDER_SUBSTITUTED_ITEM_UPC_PK PRIMARY KEY (RECORD_ID),
                                                                        CONSTRAINT FULFILLMENT_ORDER_SUBSTITUTED_ITEM_UPC_FK FOREIGN KEY (SUBSTITUTED_ITEM_RECORD_ID) REFERENCES OMSCOREDB.OMSCORE.FULLFILLMENT_ORDER_SUBSTITUTED_ITEM(RECORD_ID)
);
