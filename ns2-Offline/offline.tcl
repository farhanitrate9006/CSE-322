# simulator
set ns [new Simulator]

# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          CMUPriQueue              ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy          ;# network interface type
set val(mac)          Mac/802_11               ;# MAC type
set val(rp)           DSR                      ;# ad-hoc routing protocol 

set val(as) [lindex $argv 0]  ;# area side
set val(nn) [lindex $argv 1]  ;# number of mobilenodes
set val(nf) [lindex $argv 2]  ;# number of flows

set rand_calc_val 5000
set node_min_speed 1
set node_max_speed 5
# =======================================================================

# trace file
set trace_file [open trace.tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open animation.nam w]
$ns namtrace-all-wireless $nam_file $val(as) $val(as)

# topology: to keep track of node movements
set topo [new Topography]
$topo load_flatgrid $val(as) $val(as) ;# $val(as) x $val(as) area size

# general operation director for mobilenodes
create-god $val(nn)

# user-defined functions #

proc random_coordinate {} {
    global ns val
    global ns rand_calc_val

    set temp [ expr int($rand_calc_val * rand()) % $val(as) ]
    if { $temp != 0 && $temp != $val(as) } {
        return $temp
    }
    return [ expr int(($val(as) * 0.9) * rand()) + 10 ]
}

proc random_node {} {
    global ns val
    global ns rand_calc_val
    return [ expr int($rand_calc_val * rand()) % $val(nn) ]
}

proc random_motion {} {
    global ns node_max_speed
    global ns node_min_speed
    return [ expr int(($node_max_speed - $node_min_speed) * rand()) + 1 ]
}

# configuring nodes

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
                -agentTrace ON \
                -routerTrace ON \
                -macTrace OFF \
                -movementTrace OFF

# create nodes
for {set i 0} {$i < $val(nn) } {incr i} {
    set node($i) [$ns node]
    # $node($i) random-motion 0       ;# disable random motion

    # setting coordinates for node
    $node($i) set X_ [random_coordinate]
    $node($i) set Y_ [random_coordinate]
    $node($i) set Z_ 0

    # 20 => node-size
    $ns initial_node_pos $node($i) 20

    # producing node movements with uniform random speed
    $ns at [expr int(20 * rand()) + 10] "$node($i) setdest [random_coordinate] [random_coordinate] [random_motion]"
}

# generating flows
# 1 sink, random source
set dest [random_node]

for {set i 0} {$i < $val(nf)} {incr i} {
    # looping until src & dest are different 
    while {1} {
        set src [random_node]
        if {$src != $dest} {
            break
        } 
    }

    # Setup a UDP connection #
    set udp [new Agent/UDP]
    set null [new Agent/Null]
    # attach to nodes
    $ns attach-agent $node($src) $udp
    $ns attach-agent $node($dest) $null
    # connect agents
    $ns connect $udp $null
    # setting others
    $udp set class_ $i
    $udp set fid_ $i

    # Setup a CBR over UDP connection #
    # creating application-layer traffic/flow generator
    set cbr [new Application/Traffic/CBR]
    # attach to agent
    $cbr attach-agent $udp
    $cbr set type_ CBR
    $cbr set packet_size_ 1000
    $cbr set rate_ 1mb
    $cbr set random_ false
    
    # start flow generation
    $ns at [expr int(10 * rand()) + 1] "$cbr start"
}

# End Simulation #

# Stop nodes
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at 50.0 "$node($i) reset"
}

# call final function
proc finish_simulation {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    puts "Simulation ending"
    $ns halt
}

$ns at 50.0001 "finish_simulation"
$ns at 50.0002 "halt_simulation"

# Run simulation
puts "Simulation starting"
$ns run