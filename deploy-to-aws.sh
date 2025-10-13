#!/bin/bash

# Valorant Arena - AWS Deployment Script
# This script helps deploy the application to AWS EC2

set -e  # Exit on error

echo "🚀 Valorant Arena - AWS Deployment Script"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
RDS_ENDPOINT="valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com"
AWS_REGION="eu-north-1"

# Check if .env.production exists
if [ ! -f "backend/.env.production" ]; then
    echo -e "${RED}❌ Error: backend/.env.production not found${NC}"
    echo "Please create it from backend/.env.production.example"
    exit 1
fi

echo -e "${GREEN}✓ Environment file found${NC}"

# Build backend
echo ""
echo "📦 Building backend..."
cd backend
mvn clean package -DskipTests
echo -e "${GREEN}✓ Backend built successfully${NC}"

# Check if JAR was created
if [ ! -f "target/valorant-arena-backend-1.0.0.jar" ]; then
    echo -e "${RED}❌ Error: JAR file not found${NC}"
    exit 1
fi

cd ..

# Build frontend
echo ""
echo "📦 Building frontend..."
cd frontend
npm install
npm run build
echo -e "${GREEN}✓ Frontend built successfully${NC}"
cd ..

echo ""
echo -e "${GREEN}✅ Build completed successfully!${NC}"
echo ""
echo "📋 Next steps:"
echo "1. Upload backend JAR to EC2:"
echo "   scp -i your-key.pem backend/target/valorant-arena-backend-1.0.0.jar ec2-user@your-ec2-ip:/home/ec2-user/"
echo ""
echo "2. Upload .env.production to EC2:"
echo "   scp -i your-key.pem backend/.env.production ec2-user@your-ec2-ip:/home/ec2-user/"
echo ""
echo "3. Upload frontend build to S3:"
echo "   aws s3 sync frontend/dist/valorant-arena s3://your-bucket-name/ --region ${AWS_REGION}"
echo ""
echo "4. SSH into EC2 and run:"
echo "   ssh -i your-key.pem ec2-user@your-ec2-ip"
echo "   source .env.production"
echo "   java -jar valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod"
echo ""
echo "🔗 RDS Endpoint: ${RDS_ENDPOINT}"
echo "🌍 AWS Region: ${AWS_REGION}"
