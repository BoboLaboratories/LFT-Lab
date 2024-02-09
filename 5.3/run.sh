#!/bin/bash

SNIPPETS_DIR=$1
SNIPPETS_DIR=${SNIPPETS_DIR:=snippets}

if [ -d bin ] ; then
  rm -r bin
fi

mkdir bin

javac -d bin *.java

for snippet in $SNIPPETS_DIR/* ; do
  if [ -f Output.j ] ; then
    rm Output.j
  fi

  clear
  echo ""
  echo "Snippet: $(basename ${snippet})"
  echo ""
  echo "-----------Source code-----------"
  echo "Line n  Code"
  cat -n $snippet
  echo ""
  echo ""
  echo "------------Execution------------"
  java -cp bin Translator $snippet
  if [ -f Output.j ] ; then
    java -jar jasmin.jar -d ./bin Output.j
    java -cp bin Output
  fi
  echo ""
  echo -n "Press ENTER to continue.."
  read ignored
done

rm -r bin