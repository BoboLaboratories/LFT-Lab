#!/bin/bash

SNIPPETS_DIR=$1
SNIPPETS_DIR=${SNIPPETS_DIR:=snippets}

if [ -d bin ] ; then
  rm -r bin
fi

mkdir bin

javac -d bin *.java

for snippet in $SNIPPETS_DIR/* ; do
  clear
  echo ""
  echo "Snippet: $(basename ${snippet})"
  echo ""
  echo "-----------Source code-----------"
  echo "Line n  Code"
  cat -n $snippet
  echo ""
  echo ""
  echo "-------------Parsing-------------"
  java -cp bin Parser $snippet
  echo ""
  echo -n "Press ENTER to continue.."
  read ignored
done

rm -r bin