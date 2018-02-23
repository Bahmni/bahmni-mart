# bahmni-analytics

[![Build Status](https://travis-ci.org/bahmni-msf/bahmni-analytics.svg?branch=master)](https://snap-ci.com/Bahmni/bahmni-endtb-batch/branch/master)  &nbsp;&nbsp;[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b9c2ce06aa2844b99beccd05746b98bf)](https://www.codacy.com/app/sumanmaity112/bahmni-analytics?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=bahmni-msf/bahmni-analytics&amp;utm_campaign=Badge_Grade)

Standalone Batch Application based on spring-batch. This application will create various table in **analytics** DB like Patient Information, Program Enrollment Information, Drug Orders (TB and Non-TB), Various forms filled by the users (Observation Templates), Bacteriology forms information

### Dev Setup
To setup dev box please run the following command
* ```sh scripts/dev-setup.sh```

### Test
To run the test run the following command
* ```./gradlew test```
### Build RPM
To build RPM run the following command
* ```./gradlew buildRPM```
 
