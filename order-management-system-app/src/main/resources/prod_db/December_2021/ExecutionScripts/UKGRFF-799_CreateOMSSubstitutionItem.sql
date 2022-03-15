CREATE TABLE OMSCOREDB.OMSCORE.OMS_SUBSTITUTED_ITEM (
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
                                                      TOTAL_PRICE float NULL,
                                                      UNIT_PRICE float NULL,
                                                      ADJUSTED_PRICE_EX_VAT float NULL,
                                                      ADJUSTED_PRICE float NULL,
                                                      WEB_ADJUSTED_PRICE float NULL,
                                                      VENDOR_UNIT_PRICE float NULL,
                                                      VAT_AMOUNT float NULL,
                                                      INFLATION_RATE float NULL,
                                                      PICKED_ITEM_RECORD_ID uniqueidentifier NOT NULL,
                                                      CONSTRAINT OMS_SUBSTITUTED_ITEM_PK PRIMARY KEY (RECORD_ID),
                                                      CONSTRAINT OMS_SUBSTITUTED_ITEM_FK FOREIGN KEY (PICKED_ITEM_RECORD_ID) REFERENCES OMSCOREDB.OMSCORE.OMS_PICKED_ITEM(RECORD_ID)
);

CREATE TABLE OMSCOREDB.OMSCORE.OMS_SUBSTITUTED_ITEM_UPC (
                                                            RECORD_ID uniqueidentifier NOT NULL,
                                                            CREATED_DATE datetime2 NULL,
                                                            MODIFIED_DATE datetime2 NULL,
                                                            DB_LOCK_VERSION bigint NULL,
                                                            UPC varchar(30) NOT NULL,
                                                            UOM varchar(30) NULL,
                                                            SUB_ITEM_RECORD_ID uniqueidentifier NOT NULL,
                                                            CONSTRAINT OMS_SUBSTITUTED_ITEM_UPC_PK PRIMARY KEY (RECORD_ID),
                                                            CONSTRAINT OMS_SUBSTITUTED_ITEM_UPC_FK FOREIGN KEY (SUB_ITEM_RECORD_ID) REFERENCES OMSCOREDB.OMSCORE.OMS_SUBSTITUTED_ITEM(RECORD_ID)
)