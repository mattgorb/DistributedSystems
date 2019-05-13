#!/bin/bash
echo "#####################"
echo " Start $1 processes "
echo "#####################"

SCRIPT="cd ~/assignment2/build/classes/java/main/;
 java cs455.scaling.client.Client 129.82.44.165 50000 5; "

SCRIPTKILL="killall -9 -u mgorb"

for ((  i = 1 ;  i <= $1;  i++  ))
do
    #echo "Starting process: $i "

    ssh -i ~/.ssh/bouncy mgorb@raleigh.cs.colostate.edu $SCRIPT &
    sleep .15
done
echo "Running for 250 seconds"
sleep 60

ssh -i ~/.ssh/bouncy mgorb@raleigh.cs.colostate.edu $SCRIPTKILL &