app register --name bahmni-mart --type task --uri file:///opt/bahmni-mart/bahmni-mart.jar
task create create-bahmni-mart --definition bahmni-mart\ --spring.config.location=/opt/bahmni-mart/conf/\ --spring.profiles.active=prod
