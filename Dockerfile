FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

ENV JAVA_OPTS="\
  -Xms512M \
  -Xmx1024M"

CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments=\"${JAVA_OPTS}\""]