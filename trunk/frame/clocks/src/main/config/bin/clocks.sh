#!/bin/bash

cd `dirname $0`

java -cp draw-utils-1.0-SNAPSHOT.jar:clocks-1.0-SNAPSHOT.jar com.mathias.clocks.Clocks >/dev/null
