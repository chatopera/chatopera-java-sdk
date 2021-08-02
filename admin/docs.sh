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
mvn clean site:site
mvn compile javadoc:javadoc
echo "API Docs is built successfully. path:" $rootDir/"target/site/apidocs"

if [ -d ~/chatopera/chatopera-sample-java/docs ]; then
  echo "Move docs into ~/chatopera/chatopera-sample-java/docs ..."
  rm -rf ~/chatopera/chatopera-sample-java/docs
  mkdir ~/chatopera/chatopera-sample-java/docs
  cd target/site
  tar cf - .|(cd ~/chatopera/chatopera-sample-java/docs;tar xf -)
fi

