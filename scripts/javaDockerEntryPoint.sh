nohup java -Xms768m -Xmx2048m -Djava.security.egd=file:/dev/./urandom -jar /spring-cloud-dataflow-server.jar &>/dev/null &
while ! nc -z localhost 9393; do
    sleep 1;
done

java -jar spring-cloud-dataflow-shell.jar --spring.shell.commandFile=/opt/bahmni-mart/scripts/addTask.sh
echo 'Task apps imported'

while true; do
    sleep 1;
done;