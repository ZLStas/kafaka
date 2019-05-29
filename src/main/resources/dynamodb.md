#Creation of Temperature table using awscli
 aws --profile={ProfileName} dynamodb create-table --cli-input-json file://TemperatureMapping.json