#!/bin/bash

# run all the experiments once.
# and fetch the results.

cd ~/group-5/pa1/

echo "***Starting experiments..."

echo "**Weak 1..."
bash scripts/peerExp.sh weak 1
echo "**Weak 2..."
bash scripts/peerExp.sh weak 2
echo "**Weak 4..."
bash scripts/peerExp.sh weak 4

echo "**Strong_small 2..."
bash scripts/peerExp.sh strong_small 2
echo "**Strong_small 4..."
bash scripts/peerExp.sh strong_small 4

echo "**Strong_medium 2..."
bash scripts/peerExp.sh strong_medium 2
echo "**Strong_medium 4..."
bash scripts/peerExp.sh strong_medium 4

echo "***Experiments done."
echo "***Fetching results..."
bash scripts/fetchData.sh
echo "***Results fetched. (~/group-5/pa1/data)"

echo "***DONE***"
