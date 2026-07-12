#!/bin/bash
# ============================================================
# Deploy Coding Agent Harness to GitHub Container Registry (ghcr.io)
# ============================================================
# Prerequisites:
#   1. Docker installed
#   2. GitHub CLI (gh) installed and authenticated
#   3. You have write access to the repository
#
# Usage:
#   chmod +x scripts/deploy.sh
#   ./scripts/deploy.sh
# ============================================================

set -e

# Configuration
REPO_OWNER="njuer-bin"
REPO_NAME="ai-coding-harness"
IMAGE_NAME="ghcr.io/${REPO_OWNER}/${REPO_NAME}:latest"

echo "=== Building Docker image ==="
cd "$(dirname "$0")/../untitled"

# Step 1: Build the JAR
echo ">>> Building fat JAR..."
mvn package -DskipTests

# Step 2: Build Docker image
echo ">>> Building Docker image..."
docker build -t ${IMAGE_NAME} .

# Step 3: Push to GitHub Container Registry
echo ">>> Logging into ghcr.io..."
echo "NOTE: You need a GitHub Personal Access Token with 'write:packages' scope"
echo "      Login using: echo \$GITHUB_TOKEN | docker login ghcr.io -u ${REPO_OWNER} --password-stdin"
docker login ghcr.io

echo ">>> Pushing image..."
docker push ${IMAGE_NAME}

echo ""
echo "=== Deployment Complete ==="
echo "Image: ${IMAGE_NAME}"
echo ""
echo "To run locally:"
echo "  docker run -p 8080:8080 --rm ${IMAGE_NAME} --server --port=8080"
echo ""
echo "To deploy on Render.com:"
echo "  1. Go to https://render.com"
echo "  2. Click 'New +' → 'Web Service'"
echo "  3. Connect your GitHub repo"
echo "  4. Select 'Docker' environment"
echo "  5. Set start command: java -jar /app/coding-agent.jar --server --port=8080"
echo "  6. Deploy!"
echo ""
echo "To deploy on Railway.app:"
echo "  1. Go to https://railway.app"
echo "  2. Click 'New Project' → 'Deploy from GitHub repo'"
echo "  3. Select this repo"
echo "  4. Set start command: java -jar /app/coding-agent.jar --server --port=8080"
echo "  5. Deploy!"