set ns [new Simulator]

# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy/802_15_4 ;# network interface type
set val(mac)          Mac/802_15_4             ;# MAC type
set val(rp)           DSDV                     ;# ad-hoc routing protocol 

set val(energy_model) EnergyModel
set val(rx_power)     1.0
set val(tx_power)     1.0
set val(idle_power)   0.01
set val(sleep_power)  0.05         
set val(init_energy)  1000                        ;# initial energy in joules

set val(dimension)    500                       ;# h = w = dimension
set val(modify)       [lindex $argv 0]          ;# 1 to use modified algorithm, 0 to use legacy algorithm
set val(nn)           [lindex $argv 1]          ;# number of mobilenodes
set val(nf)           [lindex $argv 2]          ;# number of flows
set val(packet_rate)  [lindex $argv 3]          ;# packets per second
set val(node_speed)   [lindex $argv 4]          ;# speed of mobile nodes

set rand              [new RNG]
set sim_start         0.1
set sim_end           25.0

Agent/TCP set use_rto_modified $val(modify);
# Agent/TCP set maxcwnd_   $val(packet_rate)          ;#! not sure about this
# =======================================================================

# trace and nam files
set trace_file [open trace.tr w]
set nam_file [open animation.nam w]
$ns trace-all $trace_file
$ns namtrace-all-wireless $nam_file $val(dimension) $val(dimension)

# topology
set topo [new Topography]
$topo load_flatgrid $val(dimension) $val(dimension)

# general operation director for mobile nodes
create-god $val(nn)

$ns node-config -adhocRouting $val(rp) \
                -llType $val(ll) \
                -macType $val(mac) \
                -ifqType $val(ifq) \
                -ifqLen $val(ifqlen) \
                -antType $val(ant) \
                -propType $val(prop) \
                -phyType $val(netif) \
                -topoInstance $topo \
                -channelType $val(chan) \
                -energyModel $val(energy_model) \
                    -rxPower $val(rx_power) \
                    -txPower $val(tx_power) \
                    -idlePower $val(idle_power) \
                    -sleepPower $val(sleep_power) \
                    -initialEnergy $val(init_energy) \
                -agentTrace ON \
                -routerTrace ON \
                -macTrace OFF \
                -movementTrace OFF
                
# get the number of rows and cols
set cols         [ expr int(sqrt($val(nn))) ]
while { $val(nn) % $cols != 0 } {
    set cols     [expr $cols - 1]
}
set rows         [ expr $val(nn) / $cols ]


# create nodes
for {set i 0} {$i < $val(nn) } {incr i} {
    set node($i)        [$ns node]
    set dx              [expr $val(dimension)/$cols]
    set dy              [expr $val(dimension)/$rows]
    $node($i) random-motion 0       ;# disable random motion

    $node($i) set X_    [expr $dx * ($i % $cols)]
    $node($i) set Y_    [expr $dy * ($i / $cols)] 
    $node($i) set Z_ 0

    $ns initial_node_pos $node($i) 20
} 


# configuring flows
for {set i 0} {$i < $val(nf)} {incr i} {  
    set src_index       [expr int([$rand uniform 0 $val(nn)])]
    set dst_index       [expr int([$rand uniform 0 $val(nn)])]
    while {$dst_index == $src_index} {
        set dst_index   [expr int([$rand uniform 0 $val(nn)])]
    }

    set tcp             [new Agent/TCP]
    set sink            [new Agent/TCPSink]

    $ns attach-agent    $node($src_index) $tcp
    $ns attach-agent    $node($dst_index) $sink
    $ns connect         $tcp $sink
    # $tcp set maxseq_    val(packet_rate)
    $tcp set fid_       $i
    $tcp set packetSize_ 512
    $tcp set window_ $val(packet_rate)
    
    set ftp             [new Application/FTP]
    $ftp attach-agent   $tcp 
    $ns at $sim_start   "$ftp start"
}

for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at $sim_end     "$node($i) reset"
}

# call final function
proc finish {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    $ns halt
}

$ns at [expr $sim_end + .0001] "finish"
$ns at [expr $sim_end + .0002] "halt_simulation"

# Run simulation
$ns run