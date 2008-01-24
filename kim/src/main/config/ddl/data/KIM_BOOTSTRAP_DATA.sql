INSERT INTO KIM_NAMESPACES_T (ID, NAME, DESCRIPTION) VALUES (1, 'KIM', 'This record represents the actual KIM system and must always be loaded by default in order for the system to work properly.') 
/
INSERT INTO KIM_PERSONS_T (ID, USERNAME, PASSWORD) VALUES (1, 'admin', 'admin')
/
INSERT INTO FP_DOC_TYPE_T values ('KPMD', SYS_GUID(), 1, 'KR', 'PRINCIPAL', 'N', 'Y', 'N', 0, 'N', 'N')
/
