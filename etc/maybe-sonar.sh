#!/bin/sh

skip_unless()
{
    if ! [ "$@" ]; then
        echo "condition" "$@" "failed, skipping" >&2
        exit 0
    fi
}

skip_unless "$TRAVIS_REPO_SLUG" = grouplens/grapht
skip_unless "$TRAVIS_BRANCH" = master
skip_unless "$TRAVIS_PULL_REQUEST" = false
skip_unless "$TRAVIS_JDK_VERSION" = oraclejdk8

exec mvn --batch-mode sonar:sonar -Dsonar.login="$SONAR_TOKEN"
