#!/bin/bash
for filename in *; do \
  mongoimport --host storage.datapun.net --port 27017 \
  --db newsgroups --collection messages --file $filename; \
done
