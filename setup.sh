#!/bin/bash

set -e

echo "🚀 Setting up FLW Mobile App..."

# Step 1 - Setup .env
if [ ! -f .env ]; then
  echo "📄 .env not found! Copying from .env.example..."
  cp .env.example .env
  echo "✅ .env created! Open it and fill in ENCRYPTED_PASS_KEY, ABHA_CLIENT_SECRET, ABHA_CLIENT_ID"
fi

# Step 2 - Setup native-lib.cpp
if [ ! -f app/src/main/cpp/native-lib.cpp ]; then
  echo "📄 native-lib.cpp not found! Copying from example..."
  cp app/src/main/cpp/native-lib.cpp.example app/src/main/cpp/native-lib.cpp
  echo "✅ native-lib.cpp created!"
fi

# Step 3 - Copy dummy google-services.json
echo "📄 Copying dummy google-services.json..."
cp dummy/google-services.json app/src/sakshamUat/google-services.json
echo "✅ google-services.json copied!"

# Step 4 - Load .env variables
echo "📦 Loading environment variables..."
export $(grep -v '^#' .env | xargs)

# Step 5 - Build
echo "🔨 Starting Gradle build..."
./gradlew clean installSakshamUatDebug

echo "✅ Done! APK is ready."