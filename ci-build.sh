#!/bin/sh

set -e

cleanup()
{
    mvn build-helper:remove-project-artifact
}
trap cleanup 0 INT TERM QUIT

export CI=true
set -x
mvn -U deploy
mvn sonar:sonar
