#!/bin/sh

exec java -jar `dirname $0`/../library/logtool-*.jar $*

exit 0
