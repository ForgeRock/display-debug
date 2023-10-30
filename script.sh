#!/bin/sh
/Users/kcarron/Downloads/tomcat/bin/shutdown.sh
cd /Users/kcarron/Repositories/display-debug/
mvn clean install
rm -f /Users/kcarron/Downloads/tomcat/logs/*
rm /Users/kcarron/Downloads/tomcat/webapps/openam/WEB-INF/lib/Helper-0.0.8.jar
cp /Users/kcarron/Repositories/display-debug/target/Helper-0.0.9.jar /Users/kcarron/Downloads/tomcat/webapps/openam/WEB-INF/lib/
sleep 3
/Users/kcarron/Downloads/tomcat/bin/startup.sh

tail -f /Users/kcarron/Downloads/tomcat/logs/catalina.out