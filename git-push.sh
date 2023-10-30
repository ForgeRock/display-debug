#!/bin/sh
cd /Users/kcarron/Repositories/display-debug/
git add .
git commit -m "Can't write to anything but shared state. Everything but shared state now displays the data and doesn't accept user input. Need to add config to display data or not but it is not working on my local machine for some reason"
git push -u origin sharedState