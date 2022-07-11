#! /bin/bash 
###########################################
#
###########################################

# constants
baseDir=$(cd `dirname "$0"`;pwd)
REPO_ID_SNP=chatopera-snapshots
REPO_URL_SNP=https://nexus.chatopera.com/repository/maven-snapshots/
REPO_ID_REL=chatopera-releases
REPO_URL_REL=https://nexus.chatopera.com/repository/maven-releases/

# functions

# main 
[ -z "${BASH_SOURCE[0]}" -o "${BASH_SOURCE[0]}" = "$0" ] || return
cd $baseDir/..
mvn clean package -Dmaven.test.skip=true 
PACKAGE_VERSION=$(grep --max-count=1 '<version>' pom.xml | awk -F '>' '{ print $2 }' | awk -F '<' '{ print $1 }')

if [[ $PACKAGE_VERSION == *SNAPSHOT ]]; then
    echo "Deploy as snapshot package ..."
    set -x
    mvn deploy:deploy-file \
        -Dmaven.test.skip=true \
        -Dfile=./target/chatopera-sdk-$PACKAGE_VERSION.jar \
        -DgroupId=com.chatopera.bot \
        -DartifactId=sdk \
        -Dversion=$PACKAGE_VERSION \
        -Dpackaging=jar \
        -DgeneratePom=true \
        -DrepositoryId=$REPO_ID_SNP \
        -Durl=$REPO_URL_SNP
    if [ ! $? -eq 0 ]; then
        exit 1
    else
    	echo "Done."
    fi
else
	echo "Not snapshot"
    echo "Deploy as release package ..."
    mvn deploy:deploy-file \
        -Dmaven.test.skip=true \
        -Dfile=./target/chatopera-sdk-$PACKAGE_VERSION.jar \
        -DgroupId=com.chatopera.bot \
        -DartifactId=sdk \
        -Dversion=$PACKAGE_VERSION \
        -Dpackaging=jar \
        -DgeneratePom=true \
        -DrepositoryId=$REPO_ID_REL \
        -Durl=$REPO_URL_REL
    if [ ! $? -eq 0 ]; then
        exit 1
    fi
fi
