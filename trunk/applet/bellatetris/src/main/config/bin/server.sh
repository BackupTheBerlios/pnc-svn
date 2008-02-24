#!/bin/bash

NAME=BellaTetrisServer
PIDFILE="./server.pid"
CONTEXTPATH=".."
LIBRARY="$CONTEXTPATH/library"

start(){
  running && echo "$NAME is already running!" && return 1

  CLASSPATH="$CONTEXTPATH/config"
  for a in $LIBRARY/*.jar; do CLASSPATH=$CLASSPATH:$a; done

  /usr/bin/java -Dport=7200 -cp $CLASSPATH com.mathias.bellatetris.server.Server >/dev/null 2>&1 &

  echo $! > $PIDFILE

  running && echo "$NAME started successfully! (`cat $PIDFILE`)"
}

stop(){
  running || echo "$NAME is NOT running!" || return 1
  [ ! -f $PIDFILE ] && echo "$PIDFILE not found!" && return 1
  pid=`cat $PIDFILE`
  [ "$pid" = "" ] && echo "No process id found in $PIDFILE" && return 1
  kill $pid && rm $PIDFILE
  running || echo "$NAME successfully stopped! ($pid)"
}

running(){
  [ ! -f $PIDFILE ] && return 1
  pid=`cat $PIDFILE`
  [ "$pid" = "" ] && return 1
  running=`ps -p "$pid" -o comm=`
  [ "$running" = "" ] && return 1
  return 0
}

restart(){
  stop
  start
}

status(){
    running && echo "$NAME is running as `cat $PIDFILE`" && return 0
    running || echo "$NAME is NOT running!" || return 1
}

help(){
  echo "$0 <start | stop | restart | status | help>"
}

failfast(){
  echo $*
  exit
}

failhelp(){
  echo $*
  echo
  help
  exit
}

[ "$1" = "" ] && failhelp "No option given!"

case $1 in
    start)
    start
  ;;

    stop)
    stop
  ;;

    restart)
    restart
  ;;

    status)
    status
  ;;

    help)
    help
  ;;
esac
