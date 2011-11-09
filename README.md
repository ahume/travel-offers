Travel offers microapp that serves up context sensitve travel offers against content.

If you have an article on Italy you would expect to see an Ital travel offer.

To run change directory into the root of this project and type ./start

You should use the ./start script as APPENGINE_SDK_HOME needs to be set.

If this is the first time you type ./start the Appengine SDK will be downloaded. This can take a short while.

You can find more info on the Appengine SBT plugin here....

https://github.com/eed3si9n/sbt-appengine


The sbt plugin does not currently run the local dev server (for some reason it is commented out in the source code)
but you can run it from the root of the source directory with the command (note, not from inside sbt) ...

appengine-java-sdk-1.6.0/bin/dev_appserver.sh target/webapp

