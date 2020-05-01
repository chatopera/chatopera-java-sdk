#! /bin/bash 
###########################################
#
###########################################

# constants
baseDir=$(cd `dirname "$0"`;pwd)
rootDir=$(cd $baseDir/..;pwd)

# functions

# main 
[ -z "${BASH_SOURCE[0]}" -o "${BASH_SOURCE[0]}" = "$0" ] || return
cd $baseDir/..
rm -rf target
mvn clean compile javadoc:javadoc
echo "API Docs is built successfully. path:" $rootDir/"target/site/apidocs"

if [ -d ~/chatopera/chatopera-sample-java/docs ]; then
  cd target/site/apidocs
  tar cf - .|(cd ~/chatopera/chatopera-sample-java/docs;tar xf -)
fi

