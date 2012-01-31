#!/bin/bash

APP_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. $APP_DIR/start.sh package

$APPENGINE_SDK_HOME/bin/dev_appserver.sh target/webapp