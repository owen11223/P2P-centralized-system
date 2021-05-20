#!/bin/bash

cd /home/group5/group-5/pa1/
mkdir data

input="/home/group5/group-5/pa1/peer-host-names"
RESULT_PATH="/home/group5/group-5/pa1/result.txt"
id=0
while IFS= read -r HOST
do
    echo $HOST
    scp -i ~/.ssh/gp5-kp.pem $HOST:$RESULT_PATH data/p$id.result.txt
    ((id=id+1))
done < "$input"

