#!/usr/bin/python

import fileinput

for line in fileinput.input():
  print(line.strip().lower())
