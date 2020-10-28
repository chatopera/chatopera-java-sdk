#! /bin/bash 
###########################################
#
###########################################

# constants
baseDir=$(cd `dirname "$0"`;pwd)
export PYTHONUNBUFFERED=1
export PATH=/opt/miniconda3/envs/venv-py3/bin:$PATH

# functions

# main 
[ -z "${BASH_SOURCE[0]}" -o "${BASH_SOURCE[0]}" = "$0" ] || return
cd $baseDir/..
mvn -Dmaven.test.skip=true site:site
mvn -Dmaven.test.skip=true javadoc:javadoc
set -x
ls target/site/

echo "Sumbit target/site/ to https://github.com/chatopera/chatopera-sample-java/tree/master/docs"
