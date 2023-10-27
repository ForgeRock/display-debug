#!/bin/sh
cd /Users/kcarron/Repositories/display-debug/
git add .
git commit -m "Got username and authLevel to be put into sharedState. Realm is not getting put into shared state. Iterating through key: value pairs the first value printed out is the realm and the last value printed out is the realm with old value. Not sure about that"
git push -u origin sharedState