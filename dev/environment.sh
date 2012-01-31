#!/bin/bash

DEV_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

. $DEV_DIR/appengine-install.sh

export SBT_BOOT_DIR=$HOME/.sbt/boot/

if [ ! -d "$SBT_BOOT_DIR" ]; then
  mkdir -p $SBT_BOOT_DIR
fi