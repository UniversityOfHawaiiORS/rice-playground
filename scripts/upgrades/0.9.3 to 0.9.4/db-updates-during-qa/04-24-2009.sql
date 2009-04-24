INSERT INTO KRIM_PERM_TMPL_T(PERM_TMPL_ID, OBJ_ID, VER_NBR, NM, DESC_TXT, KIM_TYP_ID, ACTV_IND, NMSPC_CD) values ('49',	'662384B381B867A1E0404F8189D868A6','1','Send Ad Hoc Request','','5','Y','KR-NS')
/


INSERT INTO KRIM_PERM_T(PERM_ID, OBJ_ID, VER_NBR, PERM_TMPL_ID, NM, DESC_TXT, ACTV_IND, NMSPC_CD) values ('332','662384B381B967A1E0404F8189D868A6',1,'49','Send Ad Hoc Request','','Y','KR-SYS')
/
INSERT INTO KRIM_PERM_T(PERM_ID, OBJ_ID, VER_NBR, PERM_TMPL_ID, NM, DESC_TXT, ACTV_IND, NMSPC_CD) values ('333','662384B381BD67A1E0404F8189D868A6',1,'49','Send Ad Hoc Request','','Y','KR-SYS')
/
INSERT INTO KRIM_PERM_T(PERM_ID, OBJ_ID, VER_NBR, PERM_TMPL_ID, NM, DESC_TXT, ACTV_IND, NMSPC_CD) values ('334','662384B381C167A1E0404F8189D868A6',1,'49','Send Ad Hoc Request','','Y','KR-SYS')
/


INSERT INTO KRIM_ROLE_PERM_T(ROLE_PERM_ID, OBJ_ID, VER_NBR, ROLE_ID, PERM_ID, ACTV_IND) values ('618','662384B381BC67A1E0404F8189D868A6',1,'83','332','Y')
/
INSERT INTO KRIM_ROLE_PERM_T(ROLE_PERM_ID, OBJ_ID, VER_NBR, ROLE_ID, PERM_ID, ACTV_IND) values ('616','662384B381C067A1E0404F8189D868A6',1,'83','333','Y')
/
INSERT INTO KRIM_ROLE_PERM_T(ROLE_PERM_ID, OBJ_ID, VER_NBR, ROLE_ID, PERM_ID, ACTV_IND) values ('617','662384B381C467A1E0404F8189D868A6',1,'66','334','Y')
/


INSERT INTO KRIM_PERM_ATTR_DATA_T(ATTR_DATA_ID, OBJ_ID, VER_NBR, PERM_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL) values ('478','662384B381BA67A1E0404F8189D868A6','1','332','5','13','KualiDocument')
/
INSERT INTO KRIM_PERM_ATTR_DATA_T(ATTR_DATA_ID, OBJ_ID, VER_NBR, PERM_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL) values ('479','662384B381BB67A1E0404F8189D868A6','1','332','5','14','F')
/
INSERT INTO KRIM_PERM_ATTR_DATA_T(ATTR_DATA_ID, OBJ_ID, VER_NBR, PERM_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL) values ('480','662384B381BE67A1E0404F8189D868A6','1','333','5','13','KualiDocument')
/
INSERT INTO KRIM_PERM_ATTR_DATA_T(ATTR_DATA_ID, OBJ_ID, VER_NBR, PERM_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL) values ('481','662384B381BF67A1E0404F8189D868A6','1','333','5','14','K')
/
INSERT INTO KRIM_PERM_ATTR_DATA_T(ATTR_DATA_ID, OBJ_ID, VER_NBR, PERM_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL) values ('482','662384B381C267A1E0404F8189D868A6','1','334','5','13','KualiDocument')
/
INSERT INTO KRIM_PERM_ATTR_DATA_T(ATTR_DATA_ID, OBJ_ID, VER_NBR, PERM_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL) values ('483','662384B381C367A1E0404F8189D868A6','1','334','5','14','A')
/


UPDATE KRIM_GRP_MBR_T SET MBR_ID='notsys' WHERE MBR_ID='NotSys' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='notsysadm' WHERE MBR_ID='NotSysAdm' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='testadmin1' WHERE MBR_ID='TestAdmin1' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='testadmin2' WHERE MBR_ID='TestAdmin2' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='testuser1' WHERE MBR_ID='TestUser1' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='testuser2' WHERE MBR_ID='TestUser2' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='testuser3' WHERE MBR_ID='TestUser3' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='testuser4' WHERE MBR_ID='TestUser4' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='testuser5' WHERE MBR_ID='TestUser5' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='testuser6' WHERE MBR_ID='TestUser6' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='newaccountuser' WHERE MBR_ID='newAccountUser' AND MBR_TYP_CD = 'P'
/
UPDATE KRIM_GRP_MBR_T SET MBR_ID='kuluser' WHERE MBR_ID='KULUSER' AND MBR_TYP_CD = 'P'
/