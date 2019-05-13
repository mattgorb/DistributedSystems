#!/bin/bash
echo "#####################"
echo " Start $1 processes "
echo "#####################"

SCRIPT="cd assignment2/build/classes/java/main/;
 java cs455.scaling.client.Client 129.82.44.165 50000 5; "

SCRIPTKILL="killall -9 -u mgorb"

for ((  i = 1 ;  i <= $1;  i++  ))
do
    #echo "Starting process: $i "

    ssh -i  ~/.ssh/bouncy mgorb@lincoln.cs.colostate.edu $SCRIPT &
    sleep .15
    #sleep was needed because of the error:
    #ssh_exchange_identification: Connection closed by remote host
    #and ssh_exchange_identification: read: Connection reset by peer
    # found similar problem on internet: https://unix.stackexchange.com/questions/128894/ssh-exchange-identification-connection-closed-by-remote-host-not-using-hosts-d/280837#280837
    #sshd apparently can't keep up
done
echo "Running for 200 seconds"
sleep 200

ssh -i  ~/.ssh/bouncy mgorb@raleigh.cs.colostate.edu $SCRIPTKILL &

