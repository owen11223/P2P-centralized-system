#!/bin/bash

# kill all the stranded java processes in peers and server
ssh -i ~/.ssh/gp5-kp.pem -l group5 192.168.5.14 killall -9 java
ssh -i ~/.ssh/gp5-kp.pem -l group5 192.168.5.16 killall -9 java
ssh -i ~/.ssh/gp5-kp.pem -l group5 192.168.5.15 killall -9 java
ssh -i ~/.ssh/gp5-kp.pem -l group5 192.168.5.17 killall -9 java

echo "javas eliminated"
