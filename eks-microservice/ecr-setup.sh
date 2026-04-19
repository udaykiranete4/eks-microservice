#!/bin/bash
# ================================================
# ECR Setup Script - Run this ONCE before anything
# Usage: chmod +x ecr-setup.sh && ./ecr-setup.sh
# ================================================

set -e

# ── CHANGE THESE ──────────────────────────────────
AWS_REGION="us-east-1"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
# ─────────────────────────────────────────────────

echo "🚀 Setting up ECR for account: $AWS_ACCOUNT_ID in $AWS_REGION"

# Step 1: Create ECR repositories
echo ""
echo "📦 Creating ECR repositories..."

aws ecr create-repository \
  --repository-name gateway-service \
  --region $AWS_REGION \
  --image-scanning-configuration scanOnPush=true \
  --image-tag-mutability MUTABLE \
  2>/dev/null && echo "✅ gateway-service repo created" || echo "⚠️  gateway-service repo already exists"

aws ecr create-repository \
  --repository-name payment-service \
  --region $AWS_REGION \
  --image-scanning-configuration scanOnPush=true \
  --image-tag-mutability MUTABLE \
  2>/dev/null && echo "✅ payment-service repo created" || echo "⚠️  payment-service repo already exists"

aws ecr create-repository \
  --repository-name job-service \
  --region $AWS_REGION \
  --image-scanning-configuration scanOnPush=true \
  --image-tag-mutability MUTABLE \
  2>/dev/null && echo "✅ job-service repo created" || echo "⚠️  job-service repo already exists"

# Step 2: Login to ECR
echo ""
echo "🔐 Logging into ECR..."
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS \
  --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

echo "✅ ECR login successful"

# Step 3: Update K8s manifests with your account ID
echo ""
echo "📝 Updating K8s manifests with your AWS Account ID..."
find k8s/ -name "*.yaml" -exec \
  sed -i "s/YOUR_ACCOUNT_ID/$AWS_ACCOUNT_ID/g" {} \;
echo "✅ K8s manifests updated"

# Step 4: Build and push all images
echo ""
echo "🐳 Building and pushing Docker images..."
ECR_BASE="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

# Gateway
echo "Building gateway-service..."
cd gateway-service
mvn clean package -DskipTests -q
docker build -t $ECR_BASE/gateway-service:latest .
docker push $ECR_BASE/gateway-service:latest
echo "✅ gateway-service pushed"
cd ..

# Payment
echo "Building payment-service..."
cd payment-service
mvn clean package -DskipTests -q
docker build -t $ECR_BASE/payment-service:latest .
docker push $ECR_BASE/payment-service:latest
echo "✅ payment-service pushed"
cd ..

# Job
echo "Building job-service..."
cd job-service
mvn clean package -DskipTests -q
docker build -t $ECR_BASE/job-service:latest .
docker push $ECR_BASE/job-service:latest
echo "✅ job-service pushed"
cd ..

# Step 5: Update IMAGE_TAG to latest in manifests for first deploy
sed -i "s/IMAGE_TAG/latest/g" k8s/gateway/deployment.yaml
sed -i "s/IMAGE_TAG/latest/g" k8s/payment/deployment.yaml
sed -i "s/IMAGE_TAG/latest/g" k8s/job-service/deployment.yaml

echo ""
echo "═══════════════════════════════════════════"
echo "✅ ALL DONE! ECR setup complete."
echo ""
echo "Your ECR repos:"
echo "  $ECR_BASE/gateway-service:latest"
echo "  $ECR_BASE/payment-service:latest"
echo "  $ECR_BASE/job-service:latest"
echo ""
echo "Next step: Create your EKS cluster!"
echo "  eksctl create cluster --name demo-cluster --region $AWS_REGION --nodes 2 --node-type t3.medium --managed"
echo "═══════════════════════════════════════════"
