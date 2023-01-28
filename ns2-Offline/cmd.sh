#!/bin/bash

# starting by creating file for recording statistics
echo -e "\n------------------ run.sh: starting -----------------\n"
touch log.txt

# defining baseline parameters
baseline_area_side=500
baseline_nodes=40
baseline_flows=20

# generating statistics and plotting graphs 
# by running simulation with varying area size and parsing generated trace files
echo -e "Area-Size(m/side)" > log.txt
echo -e "Network-Throughtput(kilobits/sec) End-to-End-Avg-Delay(sec) Packet-Delivery-Ratio(%) Packet-Drop-Ratio(%)" >> log.txt
echo -e "------------- run.sh: varying area size -------------\n"

for((i=2; i<3; i++)); do
    area_side=`expr 250 + $i \* 250`
    echo -e $area_side >> log.txt

    echo -e "offline.tcl: running with $area_side $baseline_nodes $baseline_flows\n"
    ns offline.tcl $area_side $baseline_nodes $baseline_flows
    echo -e "\nparse.awk: running\n"
    awk -f parse.awk trace.tr >> log.txt 
done

# echo -e "plot.py: running\n"
# python plot.py

# generating statistics and plotting graphs 
# by running simulation with varying number of nodes and parsing generated trace files
# echo -e "Number-of-nodes" > log.txt
# echo -e "Network-Throughtput(kilobits/sec) End-to-End-Avg-Delay(sec) Packet-Delivery-Ratio(%) Packet-Drop-Ratio(%)" >> log.txt
# echo -e "---------- run.sh: varying number of nodes ----------\n"

# for((i=0; i<5; i++)); do
#     nodes=`expr 20 + $i \* 20`
#     echo -e $nodes >> log.txt

#     echo -e "offline.tcl: running with $baseline_area_side $nodes $baseline_flows\n"
#     ns offline.tcl $baseline_area_side $nodes $baseline_flows
#     echo -e "\nparse.awk: running\n"
#     awk -f parse.awk trace.tr >> log.txt
# done

# echo -e "plot.py: running\n"
# python plot.py

# generating statistics and plotting graphs 
# by running simulation with varying number of flows and parsing generated trace files
# echo -e "Number-of-flows" > log.txt
# echo -e "Network-Throughtput(kilobits/sec) End-to-End-Avg-Delay(sec) Packet-Delivery-Ratio(%) Packet-Drop-Ratio(%)" >> log.txt
# echo -e "---------- run.sh: varying number of flows ----------\n"

# for((i=0; i<5; i++)); do
#     flows=`expr 10 + $i \* 10`
#     echo -e $flows >> log.txt

#     echo -e "offline.tcl: running with $baseline_area_side $baseline_nodes $flows\n"
#     ns offline.tcl $baseline_area_side $baseline_nodes $flows
#     echo -e "\nparse.awk: running\n"
#     awk -f parse.awk trace.tr >> log.txt
# done

# echo -e "plot.py: running\n"
# python plot.py

# terminating by removing intermediary nam, stat, and trace files
echo -e "---------------- run.sh: terminating ----------------\n"
# rm animation.nam log.txt trace.tr