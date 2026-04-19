#!/bin/bash
# ================================================
# GitHub Secrets Setup
# Run this to add AWS secrets to your GitHub repo
# Usage: chmod +x github-secrets.sh && ./github-secrets.sh
# Requires: gh CLI installed (brew install gh)
# ================================================

# ── CHANGE THESE ──────────────────────────────────
GITHUB_REPO="YOUR_GITHUB_USERNAME/eks-microservices"
# ─────────────────────────────────────────────────

echo "🔐 Adding secrets to GitHub repo: $GITHUB_REPO"
echo ""
echo "You will be prompted for each value:"
echo ""

# Get values
read -p "Enter AWS_ACCESS_KEY_ID: " AWS_KEY
read -sp "Enter AWS_SECRET_ACCESS_KEY: " AWS_SECRET
echo ""
AWS_ACCOUNT=$(aws sts get-caller-identity --query Account --output text)

# Add to GitHub
gh secret set AWS_ACCESS_KEY_ID --body "$AWS_KEY" --repo $GITHUB_REPO
gh secret set AWS_SECRET_ACCESS_KEY --body "$AWS_SECRET" --repo $GITHUB_REPO
gh secret set AWS_ACCOUNT_ID --body "$AWS_ACCOUNT" --repo $GITHUB_REPO

echo ""
echo "✅ Secrets added to GitHub!"
echo ""
echo "Secrets added:"
echo "  ✅ AWS_ACCESS_KEY_ID"
echo "  ✅ AWS_SECRET_ACCESS_KEY"
echo "  ✅ AWS_ACCOUNT_ID = $AWS_ACCOUNT"
echo ""
echo "Now push your code to trigger the pipeline!"
echo "  git add . && git commit -m 'initial commit' && git push"
