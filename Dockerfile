# Use OpenJDK 21 base image
FROM openjdk:21-jdk-slim
# Set the working directory inside the container
WORKDIR /application
# Copy the Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .
# Make the Maven wrapper executable
RUN chmod +x mvnw
# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B
# Copy the source code
COPY src/ ./src/
# Build the application
RUN ./mvnw clean package -DskipTests
# Create a non-root user
RUN groupadd -r usermanagement && useradd -r -g usermanagement usermanagement
# Change ownership of the application directory
RUN chown -R usermanagement:usermanagement /application
# Switch to non-root user
USER usermanagement
# Expose the port that the application will run on
EXPOSE 8090
# Set JVM options for optimal container performance and memory management
ENV JAVA_OPTS="-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=70.0 \
-XX:InitialRAMPercentage=30.0 \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:+UnlockExperimentalVMOptions \
-XX:G1HeapRegionSize=16m \
-XX:+UseStringDeduplication \
-XX:+OptimizeStringConcat \
-Djava.security.egd=file:/dev/./urandom \
-Dspring.jpa.show-sql=false \
-Dlogging.level.org.hibernate.SQL=WARN"
# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/*.jar"]
