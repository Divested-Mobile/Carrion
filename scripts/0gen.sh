#!/bin/sh
#License: CC0
#Description: Carrion conversion script for https://www.ftc.gov/policy-notices/open-government/data-sets/do-not-call-data

#High confidence: past 90 data sets, number occurs at least twice
tail -n +2 *.csv old/*.csv | sort -u | sed 's/,/ , /' | awk '{ print $1 }' | grep -E '^[[:digit:]]{10}$' | grep -v -f 0exclusions.grep | sort | uniq -d | sort -u  > complaint_numbers-highconf.txt

#All numbers: past 30 data sets + high confidence
tail -n +2 *.csv | sort -u | sed 's/,/ , /' | awk '{ print $1 }' | grep -E '^[[:digit:]]{10}$' | grep -v -f 0exclusions.grep | sort -u  > complaint_numbers.tmp
cat complaint_numbers-highconf.txt complaint_numbers.tmp | sort -u > complaint_numbers.txt
rm complaint_numbers.tmp;

gzip -f complaint_numbers.txt
gzip -f complaint_numbers-highconf.txt
