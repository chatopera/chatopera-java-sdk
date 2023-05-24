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
mvn -Dmaven.test.skip=true clean site:site
mvn -Dmaven.test.skip=true compile javadoc:javadoc || true

if [ ! -d target/site/apidocs ]; then
  echo "Java docs not found"
  exit 1
fi

echo "API Docs is built successfully. path:" $rootDir/"target/site/apidocs"

if [ -d ~/chatopera/chatopera-sample-java/docs ]; then
  echo "Move docs into ~/chatopera/chatopera-sample-java/docs ..."
  rm -rf ~/chatopera/chatopera-sample-java/docs
  mkdir ~/chatopera/chatopera-sample-java/docs
  cd target/site
  tar cf - .|(cd ~/chatopera/chatopera-sample-java/docs;tar xf -)
  echo "Sumbit target/site/ to https://github.com/chatopera/chatopera-sample-java/tree/master/docs"
fi

