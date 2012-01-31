#!/bin/bash

DEV_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

APPENGINE_SDK_VERSION="1.6.1.1"

APPENGINE_SDK_FILE="appengine-java-sdk-${APPENGINE_SDK_VERSION}.zip"
APPENGINE_SDK_DOWNLOAD_URL="http://googleappengine.googlecode.com/files/${APPENGINE_SDK_FILE}"

pushd $DEV_DIR > /dev/null

APPENGINE_SDK_HOME=$PWD/$(basename ${APPENGINE_SDK_FILE} .zip)

if [ ! -e ${APPENGINE_SDK_FILE} ]; then
    echo " "
    echo " "
    echo " "
    echo "-----------------------------------------------------------------------------------------------------------"
    echo "Downloading Appengine SDK - this should only happen on first run"
    echo "$APPENGINE_SDK_DOWNLOAD_URL"
    echo "Please wait"
    echo "-----------------------------------------------------------------------------------------------------------"
    echo " "
    echo " "
    echo " "
    wget $APPENGINE_SDK_DOWNLOAD_URL
    echo " "
    echo " "
    echo "APPENGINE_SDK_HOME set to $APPENGINE_SDK_HOME"
fi

if [ -e ${APPENGINE_SDK_FILE} -a ! -e ${APPENGINE_SDK_HOME} ]; then
    unzip ${APPENGINE_SDK_FILE}
fi

export APPENGINE_SDK_HOME

popd > /dev/null
