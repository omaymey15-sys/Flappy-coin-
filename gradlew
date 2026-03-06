#!/usr/bin/env sh
##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Determine if the script is running in Windows via Cygwin or not
cygwin=false
darwin=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true ;;
esac

# Resolve links - $0 may be a symlink
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")/"$link"
  fi
done

PRGDIR=$(dirname "$PRG")
EXECUTABLE=gradle

# Set GRADLE_HOME if not already set
if [ -z "$GRADLE_HOME" ]; then
  GRADLE_HOME=$(cd "$PRGDIR/gradle/wrapper" && cd .. && pwd)
fi

# Execute Gradle
"$GRADLE_HOME"/bin/"$EXECUTABLE" "$@"
