#!/bin/sh
#License: CC0
#Description: Carrion conversion script for https://www.ftc.gov/policy-notices/open-government/data-sets/do-not-call-data

#Archive: past 360 data sets, number occurs at least ten times
tail -n +2 current30/*.csv previous60/*.csv ancient270/*.csv | sort -u | sed 's/,/ , /' | awk '{ print $1 }' | grep -E '^[[:digit:]]{10}$' | grep -v -f 0exclusions.grep | sort | uniq -c | sort | grep -v -e "      1 "  -e "      2 " -e "      3 " -e "      4 " -e "      5 " -e "      6 " -e "      7 " -e "      8 " -e "      9 " | awk '{ print $2 }' | sort -u > complaint_numbers-archive.txt;

#High confidence: past 90 data sets, number occurs at least twice + archive
tail -n +2 current30/*.csv previous60/*.csv | sort -u | sed 's/,/ , /' | awk '{ print $1 }' | grep -E '^[[:digit:]]{10}$' | grep -v -f 0exclusions.grep | sort | uniq -d | sort -u  > complaint_numbers-highconf.tmp;
cat complaint_numbers-archive.txt complaint_numbers-highconf.tmp | sort -u > complaint_numbers-highconf.txt;
rm complaint_numbers-highconf.tmp;

#All numbers: past 30 data sets + high confidence
tail -n +2 current30/*.csv | sort -u | sed 's/,/ , /' | awk '{ print $1 }' | grep -E '^[[:digit:]]{10}$' | grep -v -f 0exclusions.grep | sort -u  > complaint_numbers.tmp;
cat complaint_numbers-highconf.txt complaint_numbers.tmp | sort -u > complaint_numbers.txt;
rm complaint_numbers.tmp;

wc -l complaint_numbers*.txt;
rm complaint_numbers-archive.txt;

gzip -f complaint_numbers.txt;
gzip -f complaint_numbers-highconf.txt;
