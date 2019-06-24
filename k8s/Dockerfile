FROM openjdk:11

ENV APP_NAME kafaka

# Set the project environment
ENV PROJECT_HOME /usr/src

RUN mkdir -p $PROJECT_HOME/$APP_NAME/bin

RUN ls
# Copy the artifact that was built.
COPY  build/libs/$APP_NAME*.jar $PROJECT_HOME/$APP_NAME/$APP_NAME.jar

# Copy script for running the artifact
COPY k8s/$APP_NAME $PROJECT_HOME/$APP_NAME/bin/
RUN chmod 755 $PROJECT_HOME/$APP_NAME/bin/$APP_NAME

# Expose default port.
# HINT: If you want the server to listen on a different port do that here.
EXPOSE 9000

# Run the service using the executable.
ENTRYPOINT ["sh", \
  "-c", \
  "start.sh" \
]
