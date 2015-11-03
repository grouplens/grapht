#!/bin/sh

[ "$TRAVIS_REPO_SLUG" = grouplens/grapht ] || exit 0
[ "$TRAVIS_BRANCH" = master ] || exit 0
[ -z "$TRAVIS_PULL_REQUEST" ] || exit 0
[ "$TRAVIS_JDK_VERSION" = oraclejdk8 ] || exit 0

exec mvn --batch-mode sonar:sonar
