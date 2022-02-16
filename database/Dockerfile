FROM jacobalberty/firebird:v3.0.8

ENV ISC_PASSWORD=masterkey
COPY ./school.fbk  /firebird/data/
RUN ${PREFIX}/bin/gbak -rep -user SYSDBA -pas masterkey ${DBPATH}/school.fbk ${DBPATH}/school.fdb