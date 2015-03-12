#!/bin/sh

set -e
set -x

export CI=true
mvn -U deploy
mvn sonar:sonar
