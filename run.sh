#!/bin/bash

cd ./bin || exit 1
java $1 "../src/snippets/$2"
