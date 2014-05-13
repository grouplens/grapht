#!/bin/sh

DEPLOY_JDK=oraclejdk7

skip()
{
    echo "$@" >&2
    exit 0
}

if [ "$TRAVIS_JDK_VERSION" != "$DEPLOY_JDK" ]; then
    skip "Coverage disabled for JDK $TRAVIS_JDK_VERSION"
fi

if [ "$TRAVIS_REPO_SLUG" != "grouplens/grapht" ]; then
    skip "Coverage disabled for forks"
fi

if [ -z "$COVERALLS_TOKEN" ]; then
    echo "Coveralls token unavailable" >&2
    exit 2
fi

echo "Running Maven deploy"
exec mvn -Ptest-coverage test jacoco:report coveralls:jacoco
