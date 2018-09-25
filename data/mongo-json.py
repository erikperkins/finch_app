#!/usr/bin/python
from json import dumps
from os import listdir
from re import match
from string import join

for dir in listdir('newsgroups/'):
  for filename in listdir('newsgroups/%s' % dir):
    with open('newsgroups/%s/%s' % (dir, filename)) as file:

      contents = unicode(file.read(), errors = 'replace')
      lines = contents.split("\n")

      headers = [line for line in lines if match(r"^[-\w]+: .*$", line)]
      message = {k: v.strip() for [k, v] in [header.split(":", 1) for header in headers]}
      message['body'] = join([line for line in lines if line not in headers], "\n").strip()

    with open('mongo/%s.%s.json' % (dir, filename), 'w+') as file:
      file.write(dumps(message))
