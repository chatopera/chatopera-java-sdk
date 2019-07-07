#! /bin/bash 
###########################################
# Run tests
# https://maven.apache.org/surefire/maven-surefire-plugin/examples/single-test.html
###########################################

# constants
baseDir=$(cd `dirname "$0"`;pwd)
# functions

# main 
[ -z "${BASH_SOURCE[0]}" -o "${BASH_SOURCE[0]}" = "$0" ] || return
cd $baseDir/..
if [ $# -eq 0 ]; then
    mvn test
else
    echo "test against" $*
    # such as, ChatbotTest#testGetChatbot
    mvn -Dtest=$* test
fi


