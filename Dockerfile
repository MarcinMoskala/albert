FROM eclipse-temurin:21-jdk

# Install required packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    unzip \
    wget \
    ca-certificates \
    git \
    && rm -rf /var/lib/apt/lists/*

# Set up Android SDK
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

RUN mkdir -p /opt/android-sdk/cmdline-tools && \
    cd /opt/android-sdk && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip && \
    unzip cmdline-tools.zip -d /opt/android-sdk/cmdline-tools && \
    mv /opt/android-sdk/cmdline-tools/cmdline-tools /opt/android-sdk/cmdline-tools/latest && \
    rm cmdline-tools.zip

# Accept licenses and install minimal components
RUN yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses && \
    /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager \
      "platform-tools" \
      "platforms;android-36" \
      "build-tools;36.0.0"

WORKDIR /app
COPY . /app

# Allow passing backend URL at build time so the JS bundle gets the right endpoint
ARG SERVER_URL
ENV SERVER_URL=${SERVER_URL}

# Ensure Gradle wrapper is executable
RUN chmod +x /app/gradlew

# Gradle build (adjust command if you want to skip checks/tests differently)
RUN ./gradlew clean :server:installDist -x check -x test -Pproduction

EXPOSE 8080
CMD ["/app/server/build/install/server/bin/server"]
