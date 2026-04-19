# EKS Microservices — Full Setup Guide

## Project Structure

```
eks-microservices/
├── gateway-service/          ← Routes traffic  (port 8080)
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── payment-service/          ← Handles payments (port 8081)
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── job-service/              ← Background jobs  (port 8082)
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── k8s/
│   ├── namespace.yaml
│   ├── gateway/deployment.yaml
│   ├── payment/deployment.yaml
│   ├── job-service/deployment.yaml
│   └── ingress/ingress.yaml
├── .github/workflows/
│   └── deploy.yml            ← CI/CD pipeline
├── ecr-setup.sh              ← One-time ECR setup
└── github-secrets.sh         ← Add secrets to GitHub
```

---

## API Endpoints

### Gateway Service (port 8080)
| Method | Path     | Description         |
|--------|----------|---------------------|
| GET    | /        | Service info        |
| GET    | /health  | Liveness probe      |
| GET    | /ready   | Readiness probe     |
| GET    | /info    | Pod/env info        |

### Payment Service (port 8081)
| Method | Path           | Description         |
|--------|----------------|---------------------|
| GET    | /              | Service info        |
| GET    | /health        | Liveness probe      |
| GET    | /ready         | Readiness probe     |
| POST   | /pay           | Process payment     |
| GET    | /transactions  | List transactions   |
| GET    | /info          | Pod/env info        |

### Job Service (port 8082)
| Method | Path     | Description         |
|--------|----------|---------------------|
| GET    | /        | Service info        |
| GET    | /health  | Liveness probe      |
| GET    | /ready   | Readiness probe     |
| POST   | /jobs    | Create job          |
| GET    | /jobs    | List jobs           |
| GET    | /info    | Pod/env info        |

---

## Step-by-Step Setup

### STEP 1 — Prerequisites

```bash
# Install required tools (Mac)
brew install --cask docker
brew install java@17
brew install maven
brew install awscli
brew install kubectl
brew install eksctl
brew install helm
brew install gh              # GitHub CLI

# Verify all installed
java --version               # should be 17+
mvn --version
docker --version
aws --version
kubectl version --client
eksctl version
helm version
gh --version
```

---

### STEP 2 — Create GitHub Repo

```bash
# Option A: Using GitHub CLI
gh repo create eks-microservices --public --clone
cd eks-microservices

# Copy all project files into this folder, then:
git add .
git commit -m "initial: add all microservices"
git push origin main
```

---

### STEP 3 — Configure AWS CLI

```bash
aws configure
# Enter:
#   AWS Access Key ID:     your key
#   AWS Secret Access Key: your secret
#   Default region:        us-east-1
#   Default output format: json

# Verify
aws sts get-caller-identity
```

---

### STEP 4 — Setup ECR (One Time)

```bash
chmod +x ecr-setup.sh
./ecr-setup.sh
```

This script will:
- Create 3 ECR repositories
- Log Docker into ECR
- Build all 3 Java apps
- Push images to ECR
- Update K8s manifests with your account ID

---

### STEP 5 — Add GitHub Secrets

```bash
# Login to GitHub CLI first
gh auth login

chmod +x github-secrets.sh
./github-secrets.sh
```

Secrets needed in GitHub → Settings → Secrets:
```
AWS_ACCESS_KEY_ID      → your AWS key
AWS_SECRET_ACCESS_KEY  → your AWS secret
AWS_ACCOUNT_ID         → your 12-digit account ID
```

---

### STEP 6 — Create EKS Cluster

```bash
eksctl create cluster \
  --name demo-cluster \
  --region us-east-1 \
  --nodegroup-name workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4 \
  --managed

# Wait ~15 minutes, then verify
kubectl get nodes
```

---

### STEP 7 — Install NGINX Ingress Controller

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.service.type=LoadBalancer

# Get external IP (wait 2-3 mins)
kubectl get svc -n ingress-nginx
```

---

### STEP 8 — Deploy to EKS

```bash
# Apply all manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/gateway/
kubectl apply -f k8s/payment/
kubectl apply -f k8s/job-service/
kubectl apply -f k8s/ingress/

# Check everything is running
kubectl get all -n demo
```

---

### STEP 9 — Test Your Services

```bash
# Get the Load Balancer URL
INGRESS_URL=$(kubectl get svc ingress-nginx-controller \
  -n ingress-nginx \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

echo "Your URL: http://$INGRESS_URL"

# Test each service
curl http://$INGRESS_URL/gateway/
curl http://$INGRESS_URL/gateway/health
curl http://$INGRESS_URL/gateway/info

curl http://$INGRESS_URL/payment/
curl http://$INGRESS_URL/payment/health
curl http://$INGRESS_URL/payment/transactions

curl http://$INGRESS_URL/jobs/
curl http://$INGRESS_URL/jobs/health
curl http://$INGRESS_URL/jobs/jobs
```

---

### STEP 10 — CI/CD (Auto Deploy on Push)

Every time you push to `main`:
1. GitHub Actions builds all 3 Java apps
2. Pushes Docker images to ECR with git SHA tag
3. Updates K8s manifests with new image tag
4. Deploys to EKS
5. Waits for rollout to complete

```bash
# Trigger a deploy
git add .
git commit -m "update: new feature"
git push origin main

# Watch it in GitHub → Actions tab
```

---

## Useful Daily Commands

```bash
# Pods
kubectl get pods -n demo
kubectl get pods -n demo -o wide          # shows which node
kubectl describe pod <name> -n demo       # full details
kubectl logs <name> -n demo -f            # stream logs
kubectl logs <name> -n demo --previous    # crashed pod logs
kubectl exec -it <name> -n demo -- sh     # shell into pod

# Deployments
kubectl get deployments -n demo
kubectl rollout status deployment/gateway -n demo
kubectl rollout history deployment/gateway -n demo
kubectl rollout undo deployment/gateway -n demo   # rollback!

# Services & Ingress
kubectl get svc -n demo
kubectl get ingress -n demo

# Resource usage
kubectl top pods -n demo
kubectl top nodes

# Events (first place to look when something breaks)
kubectl get events -n demo --sort-by='.lastTimestamp'
```

---

## Troubleshooting Quick Reference

| Problem | Command | Fix |
|---|---|---|
| Pod CrashLoopBackOff | `kubectl logs <pod> --previous -n demo` | Fix app error |
| Pod Pending | `kubectl describe pod <pod> -n demo` | Check node capacity/taints |
| ImagePullBackOff | `kubectl describe pod <pod> -n demo` | Fix ECR permissions |
| Service unreachable | `kubectl get endpoints <svc> -n demo` | Fix label selector |
| OOMKilled | `kubectl describe pod <pod> -n demo` | Increase memory limit |
