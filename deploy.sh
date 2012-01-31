#!/bin/bash

APP_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. $APP_DIR/start.sh clean
. $APP_DIR/start.sh package

$APPENGINE_SDK_HOME/bin/appcfg.sh update target/webapp