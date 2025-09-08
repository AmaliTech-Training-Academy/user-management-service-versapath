# Use OpenJDK 21 base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /application

# Define build arguments for authentication
ARG GH_USER
ARG GH_TOKEN

# Pass the build arguments as environment variables
ENV GH_USER=$GH_USER
ENV GH_TOKEN=$GH_TOKEN

# Copy the Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .

# Copy settings.xml
COPY settings.xml .

# Make the Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies using settings.xml (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B -s settings.xml

# Copy the source code
COPY src/ ./src/

# Build the application using settings.xml
RUN ./mvnw clean package -DskipTests -s settings.xml

# Create a non-root user
RUN groupadd -r usermanagement && useradd -r -g usermanagement usermanagement

# Change ownership of the application directory
RUN chown -R usermanagement:usermanagement /application

# Switch to non-root user
USER usermanagement

# Expose the port that the application will run on
EXPOSE 8090

# Set JVM options for optimal container performance
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar target/*.jar"]