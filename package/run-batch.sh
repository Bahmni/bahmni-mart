#!/bin/bash

run(){
	now=$(date +%Y%m%d)
	base=${1:-/opt/bahmni-batch/output/}  
	outputFolder=${base}/${now}
	mkdir -p ${outputFolder}
	SPRING_APPLICATION_JSON='{"outputFolder":"${outputFolder}"}' java -jar bahmni-batch-0.1.0.jar >> /var/log/bahmni-batch/bahmni-batch.log 2>&1
	zip ${now}.zip ${outputFolder}
}

run $1
