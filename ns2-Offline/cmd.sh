#!/bin/bash

# starting by creating file for recording statistics
echo "=============== run.sh: starting ==============="
touch log.txt

# defining baseline parameters
baseline_area_side=500
baseline_nodes=40
baseline_flows=20

# echo "Area-Size(m/side)" > log.txt
# echo "Network-Throughtput(kilobits/sec) End-to-End-Avg-Delay(sec) Packet-Delivery-Ratio(%) Packet-Drop-Ratio(%)" >> log.txt
# echo "========== run.sh: varying area size =========="

# for((i=0; i<5; i++)); do
#     (( area_side = 250 + $i * 250 ))
#     echo $area_side >> log.txt

#     echo "offline.tcl: running with $area_side $baseline_nodes $baseline_flows"
#     ns offline.tcl $area_side $baseline_nodes $baseline_flows
#     echo "parse.awk: running"
#     awk -f parse.awk trace.tr >> log.txt 
# done

# echo "plot.py: running\n"
# python plot.py

# echo "Number-of-nodes" > log.txt
# echo "Network-Throughtput(kilobits/sec) End-to-End-Avg-Delay(sec) Packet-Delivery-Ratio(%) Packet-Drop-Ratio(%)" >> log.txt
# echo "========== run.sh: varying number of nodes =========="

# for((i=0; i<5; i++)); do
#     (( nodes = 20 + $i * 20 ))
#     echo $nodes >> log.txt

#     echo "offline.tcl: running with $baseline_area_side $nodes $baseline_flows"
#     ns offline.tcl $baseline_area_side $nodes $baseline_flows
#     echo "parse.awk: running"
#     awk -f parse.awk trace.tr >> log.txt
# done

# echo "plot.py: running\n"
# python plot.py

echo "Number-of-flows" > log.txt
echo "Network-Throughtput(kilobits/sec) End-to-End-Avg-Delay(sec) Packet-Delivery-Ratio(%) Packet-Drop-Ratio(%)" >> log.txt
echo "========== run.sh: varying number of flows =========="

for((i=0; i<5; i++)); do
    (( flows = 10 + $i * 10 ))
    echo $flows >> log.txt

    echo "offline.tcl: running with $baseline_area_side $baseline_nodes $flows"
    ns offline.tcl $baseline_area_side $baseline_nodes $flows
    echo "parse.awk: running"
    awk -f parse.awk trace.tr >> log.txt
done

# echo "plot.py: running\n"
# python plot.py

echo "=============== run.sh: terminating ==============="