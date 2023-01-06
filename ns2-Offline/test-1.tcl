set min 1
set max 5

set n [new RandomVariable/Normal]
$n set max_ $max
$n set min_ $min

for {set i 1} {$i < 11} {incr i} {
	puts "[expr int($max * rand())]"
}