#!/bin/bash

cd ./bin || exit 1

for f in ../src/snippets/* ; do
  javac -d . ../src/*.java && java Translator "$f" # && java -jar ../jasmin.jar Output.j && java Output
  if [ $? -eq 0 ] ; then
    echo "Starting program $f"
    java -jar ../jasmin.jar Output.j && java Output
  else
    echo "Could not run program, some errors were found"
  fi
done