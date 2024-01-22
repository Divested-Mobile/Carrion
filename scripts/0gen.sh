#!/bin/sh
#License: CC0
#Description: Carrion conversion script for https://www.ftc.gov/policy-notices/open-government/data-sets/do-not-call-data

tail -n +2 *.csv | sed 's/,/ , /' | awk '{ print $1 }' | grep -E '^[[:digit:]]{10}$' | grep -v -f 0exclusions.grep | sort -u  > complaint_numbers.txt
tail -n +2 *.csv | sed 's/,/ , /' | awk '{ print $1 }' | grep -E '^[[:digit:]]{10}$' | grep -v -f 0exclusions.grep | sort | uniq -d | sort -u  > complaint_numbers-highconf.txt
