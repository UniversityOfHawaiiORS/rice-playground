-- KULRICE-4517 - Needed for effective dating
CREATE TABLE TRV_ACCT_USE_RT_T ( 
    ID VARCHAR2(40) PRIMARY KEY, 
    ACCT_NUM VARCHAR2(10) NOT NULL, 
    RATE NUMBER(8) NOT NULL, 
    ACTV_FRM_DT DATE DEFAULT NULL, 
    ACTV_TO_DT DATE DEFAULT NULL
)
