#!/bin/bash

echo "**Running strong experiments**"

cd ~/group-5/pa1

# strong_small exp. on 2 machines
for i in {1..5};
do
    echo "strong_small 2 i=$i"
    bash scripts/peerExp.sh strong_small 2
    bash scripts/killjavas.sh
done

# strong_small exp. on 4 machines
for i in {1..5};
do
    echo "strong_small 4 i=$i"
    bash scripts/peerExp.sh strong_small 4
    bash scripts/killjavas.sh
done

# strong_medium exp. on 2 machines
for i in {1..5};
do
    echo "strong_medium 2 i=$i"
    bash scripts/peerExp.sh strong_medium 2
    bash scripts/killjavas.sh
done

# strong_medium exp. on 4 machines
for i in {1..5};
do
    echo "strong_medium 4 i=$i"
    bash scripts/peerExp.sh strong_medium 4
    bash scripts/killjavas.sh
done

echo "**strong experiments done**"
