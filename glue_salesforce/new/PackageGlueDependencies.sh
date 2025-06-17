#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Define variables
VENV_DIR="glue-env"
LIB_DIR="glue-libs"
ZIP_FILE="glue_dependencies.zip"
S3_BUCKET="your-bucket-name"
S3_KEY="glue_dependencies.zip"

# Create virtual environment
python3 -m venv $VENV_DIR
source $VENV_DIR/bin/activate

# Create directory for dependencies
mkdir -p $LIB_DIR

# Install required packages into the local directory
pip install pyjwt requests cryptography -t $LIB_DIR

# Zip the dependencies
cd $LIB_DIR
zip -r ../$ZIP_FILE .
cd ..

# Upload to S3
aws s3 cp $ZIP_FILE s3://$S3_BUCKET/$S3_KEY

echo "Dependencies packaged and uploaded to s3://$S3_BUCKET/$S3_KEY"
