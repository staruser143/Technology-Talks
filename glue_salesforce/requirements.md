pyjwt
cryptography
requests
boto3

# Create Python environment to install the packages
mkdir glue-libs
pip install pyjwt cryptography requests -t glue-libs/



# Zip the contents
cd glue-libs
zip -r ../glue-libs.zip .


# Upload glue-libs.zip to s3
aws s3 cp glue-libs.zip s3://your-glue-script-bucket/dependencies/


default_arguments = {
  "--additional-python-modules" = "pyjwt,cryptography,requests"
}

2. Reference it in the glue job configuration or terraform
default_arguments = {
  "--extra-py-files" = "s3://your-glue-script-bucket/dependencies/glue-libs.zip"
}

# Use AWS Glue Python Library Parameters
default_arguments = {
  "--additional-python-modules" = "pyjwt,cryptography,requests"
}

