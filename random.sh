#!/bin/bash
awk 'BEGIN {srand()} !/^$/ { if (rand() <= .01) print $0}'
