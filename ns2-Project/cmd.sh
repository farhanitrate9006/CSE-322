#!/bin/bash

# starting by creating file for recording statistics
echo "=============== run.sh: starting ==============="

# defining baseline parameters
nodes_default=40
flows_default=20
packet_default=200
speed_default=10
unmodified=0
modified=1
tcl_file_to_run="wireless-802.11.tcl"

print_parameters() {
    echo $1 > log_1.txt
    echo $1 > log_2.txt
    echo "Network-Throughtput(kilobits/sec) End-to-End-Avg-Delay(sec) Packet-Delivery-Ratio(%) Packet-Drop-Ratio(%) Energe-Consumption(J)" >> log_1.txt
    echo "Network-Throughtput(kilobits/sec) End-to-End-Avg-Delay(sec) Packet-Delivery-Ratio(%) Packet-Drop-Ratio(%) Energe-Consumption(J)" >> log_2.txt
}

print_first_line() {
    echo $1 >> log_1.txt
    echo $1 >> log_2.txt
}

print_parameters "Nodes"
echo "========== run.sh: varying number of nodes =========="
for((i=0; i<5; i++)); do
    (( nodes = 20 + $i * 20 ))
    print_first_line $nodes

    echo "$tcl_file_to_run unmodified running with $nodes $flows_default $packet_default $speed_default"
    ns $tcl_file_to_run $unmodified $nodes $flows_default $packet_default $speed_default >> check.txt
    echo "parse.awk: running"
    awk -f parse.awk trace.tr >> log_1.txt

    echo "$tcl_file_to_run modified running with $nodes $flows_default $packet_default $speed_default"
    ns $tcl_file_to_run $modified $nodes $flows_default $packet_default $speed_default >> check.txt
    echo "parse.awk: running"
    awk -f parse.awk trace.tr >> log_2.txt
done
# echo "plot.py: running\n"
# python plot.py


# print_parameters "Flows"
# echo "========== run.sh: varying number of flows =========="
# for((i=0; i<5; i++)); do
#     (( flows = 10 + $i * 10 ))
#     print_first_line $flows

#     echo "$tcl_file_to_run unmodified running with $nodes_default $flows $packet_default $speed_default"
#     ns $tcl_file_to_run $unmodified $nodes_default $flows $packet_default $speed_default >> check.txt
#     echo "parse.awk: running"
#     awk -f parse.awk trace.tr >> log_1.txt

#     echo "$tcl_file_to_run modified running with $nodes_default $flows $packet_default $speed_default"
#     ns $tcl_file_to_run $modified $nodes_default $flows $packet_default $speed_default >> check.txt
#     echo "parse.awk: running"
#     awk -f parse.awk trace.tr >> log_2.txt
# done
# echo "plot.py: running\n"
# python plot.py


# print_parameters "Packets"
# echo "========== run.sh: varying number of packets =========="
# for((i=0; i<5; i++)); do
#     (( packets = 100 + $i * 100 ))
#     print_first_line $packets

#     echo "$tcl_file_to_run unmodified running with $nodes_default $flows_default $packets $speed_default"
#     ns $tcl_file_to_run $unmodified $nodes_default $flows_default $packets $speed_default >> check.txt
#     echo "parse.awk: running"
#     awk -f parse.awk trace.tr >> log_1.txt

#     echo "$tcl_file_to_run modified running with $nodes_default $flows_default $packets $speed_default"
#     ns $tcl_file_to_run $modified $nodes_default $flows_default $packets $speed_default >> check.txt
#     echo "parse.awk: running"
#     awk -f parse.awk trace.tr >> log_2.txt
# done
# # echo "plot.py: running\n"
# # python plot.py


# print_parameters "Speed"
# echo "========== run.sh: varying number of speed =========="
# for((i=0; i<5; i++)); do
#     (( speed = 5 + $i * 5 ))
#     print_first_line $speed

#     echo "$tcl_file_to_run unmodified running with $nodes_default $flows_default $packet_default $speed"
#     ns $tcl_file_to_run $unmodified $nodes_default $flows_default $packet_default $speed >> check.txt
#     echo "parse.awk: running"
#     awk -f parse.awk trace.tr >> log_1.txt

#     echo "$tcl_file_to_run modified running with $nodes_default $flows_default $packet_default $speed"
#     ns $tcl_file_to_run $modified $nodes_default $flows_default $packet_default $speed >> check.txt
#     echo "parse.awk: running"
#     awk -f parse.awk trace.tr >> log_2.txt
# done
# echo "plot.py: running\n"
# python plot.py


echo "=============== run.sh: terminating ==============="