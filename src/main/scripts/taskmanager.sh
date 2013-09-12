#!/bin/sh
#Author: yanwei

JAVA_HOME="/usr/java/jdk1.6.0_32"
RUNNING_USER=root
CURRENT_DIR=`pwd`
APP_HOME=`dirname $CURRENT_DIR`
APP_NAME=tase-taskmanager-2.0-SNAPSHOT.jar
LOG_NAME=taskmanager.log
 
CLASSPATH=$APP_HOME
for i in "$APP_HOME"/lib/*.*; do
   CLASSPATH="$CLASSPATH":"$i"
done
CLASSPATH="$APP_HOME":"CLASSPATH"

JAVA_OPTS="-ms512m -mx512m -Xmn256m -XX:MaxPermSize=128m -Djava.library.path=$APP_HOME/sigar-lib/"
 
pid=0
 
checkpid() {
   javaps=`$JAVA_HOME/bin/jps -l | grep $APP_NAME`
 
   if [ -n "$javaps" ]; then
      pid=`echo $javaps | awk '{print $1}'`
   else
      pid=0
   fi
}
 
start() {
   checkpid
 
   if [ $pid -ne 0 ]; then
      echo "================================"
      echo "warn: $APP_NAME already started! (pid=$pid)"
      echo "================================"
   else
      echo -n "Starting $APP_NAME ..."
      JAVA_CMD="nohup $JAVA_HOME/bin/java $JAVA_OPTS -classpath $CLASSPATH -jar $APP_HOME/$APP_NAME >$APP_HOME/logs/$LOG_NAME 2>&1 &"
      su - $RUNNING_USER -c "$JAVA_CMD"
      checkpid
      if [ $pid -ne 0 ]; then
         echo "(pid=$pid) [OK]"
      else
         echo "[Failed]"
      fi
   fi
}
 
stop() {
   checkpid
 
   if [ $pid -ne 0 ]; then
      echo -n "Stopping $APP_NAME ...(pid=$pid) "
      su - $RUNNING_USER -c "kill -9 $pid"
      if [ $? -eq 0 ]; then
         echo "[OK]"
      else
         echo "[Failed]"
      fi
 
      checkpid
      if [ $pid -ne 0 ]; then
         stop
      fi
   else
      echo "================================"
      echo "warn: $APP_NAME is not running"
      echo "================================"
   fi
}
 
status() {
   checkpid
 
   if [ $pid -ne 0 ];  then
      echo "$APP_NAME is running! (pid=$pid)"
   else
      echo "$APP_NAME is not running"
   fi
}
 
info() {
   echo "System Information:"
   echo "****************************"
   echo `head -n 1 /etc/issue`
   echo `uname -a`
   echo
   echo "JAVA_HOME=$JAVA_HOME"
   echo `$JAVA_HOME/bin/java -version`
   echo
   echo "APP_HOME=$APP_HOME"
   echo "APP_NAME=$APP_NAME"
   echo "****************************"
}
 
case "$1" in
	'start')
		start
		;;
	'stop')
		stop
		;;
	'restart')
		stop
		start
		;;
	'status')
		status
		;;
	'info')
		info
		;;
	*)
		echo "Usage: $0 {start|stop|restart|status|info}"
		exit 1
esac