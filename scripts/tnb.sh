#!/bin/bash

# peer hostnames. assigned peerids, 0-3.
input="/home/group5/group-5/pa1/peer-host-names"

# Central server ip.
ip="192.168.5.12"

DEST="/home/group5/"
echo "rsync project directory to peers.."

while IFS= read -r HOST
do
  echo $HOST:$DEST
  rsync -az -e "ssh -i /home/group5/.ssh/gp5-kp.pem" ~/group-5 $HOST:$DEST
done < "$input"

#rsync -az -e "ssh -i /home/group5/.ssh/gp5-kp.pem" ~/group-5 group5@192.168.5.14:/home/group5/
#rsync -az -e "ssh -i /home/group5/.ssh/gp5-kp.pem" ~/group-5 group5@192.168.5.16:/home/group5/
#rsync -az -e "ssh -i /home/group5/.ssh/gp5-kp.pem" ~/group-5 group5@192.168.5.15:/home/group5/
#rsync -az -e "ssh -i /home/group5/.ssh/gp5-kp.pem" ~/group-5 group5@192.168.5.17:/home/group5/
echo "rsync done."


echo "Setup peer.config file..."
id=0
while IFS= read -r HOST
do
    echo $HOST $id
    ssh -i "/home/group5/.ssh/gp5-kp.pem" -n $HOST "
    cd /home/group5/group-5/pa1;
    echo 'peerid=$id' > peer.config;
    echo 'serverip=$ip' >> peer.config;
    cat peer.config; 
"
    ((id=id+1))    
done < "$input"
echo "peer.config setup done"


echo "pssh clean and build.."
pssh -h peer-host-names -x "-i ~/.ssh/gp5-kp.pem" -t 0 "
     /snap/bin/ant -f ~/group-5/pa1/build.xml clean;
     /snap/bin/ant -f ~/group-5/pa1/build.xml;
     rm -rf ~/group-5/pa1/files/dl;
     mkdir ~/group-5/pa1/files/dl
"
echo "build done."
