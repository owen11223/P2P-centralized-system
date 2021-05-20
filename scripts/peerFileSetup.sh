#!/bin/bash

cd ~/group-5/pa1/
# peer configs
. ./peer.config

ant -f build.xml clean
ant -f build.xml

# ensure the directories are there
mkdir files
mkdir files/dl
mkdir files/arch
cd files/arch
# create the files
# create files 100k-10KB, 1k-1MB, 10-100MB
count=0
while [ $count -lt 100000 ]
do
    fn=s.$peerid.$count.txt
    dd if=/dev/zero of=$fn bs=10KB count=1
    ((count++))
done

count=0
while [ $count -lt 1000 ]
do
    fn=m.$peerid.$count.txt
    dd if=/dev/zero of=$fn bs=1MB count=1
    ((count++))
done

count=0
while [ $count -lt 10 ]
do
    fn=l.$peerid.$count.txt
    dd if=/dev/zero of=$fn bs=100MB count=1
    ((count++))
done

cd ..
ls arch > flist.txt

