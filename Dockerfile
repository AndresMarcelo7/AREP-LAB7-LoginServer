FROM openjdk:8

WORKDIR /logic/bin

ENV PORT 6000
ENV TZ America/Bogota

COPY /target/classes /logic/bin/classes
COPY /target/dependency /logic/bin/dependency
RUN mkdir keystores
COPY keystores/* /logic/bin/keystores/

CMD ["java","-cp","./classes:./dependency/*","edu.eci.arep.SecureSparkApp.SecureSparkServices"]