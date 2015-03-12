#!/bin/sh

set -e
set -x

mvn -U -Ppublish-ci deploy
mvn -U -Ppublish-ci sonar:sonar
