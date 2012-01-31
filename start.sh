#!/bin/bash

APP_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. $APP_DIR/dev/environment.sh

java -Xmx768M -XX:+UseCompressedOops -XX:MaxPermSize=250m \
    -Dhttp.proxyHost=devscreen.gudev.gnl -Dhttp.proxyPort=3128 \
    -jar $APP_DIR/dev/sbt-launch-0.11.2.jar  "$@"


