import matplotlib.pyplot as plt 

# reading statistics from log.txt
log_file = open("log.txt", "r")
parameter = log_file.readline()
metrics = log_file.readline().split()

parameters = []
network_throughputs = []
end_to_end_avg_delays = []
packet_delivery_ratios = []
packet_drop_ratios = []

for line in log_file:
    if len(line.split()) == 1:
        parameters.append(int(line))
    else:
        metrics_list = line.split()
        network_throughputs.append(float(metrics_list[0]))
        end_to_end_avg_delays.append(float(metrics_list[1]))
        packet_delivery_ratios.append(float(metrics_list[2]))
        packet_drop_ratios.append(float(metrics_list[3]))

log_file.close()

# plotting graphs
plt.plot(parameters, network_throughputs, marker="^", color="b")
plt.xlabel(parameter)
plt.ylabel(metrics[0])
plt.show()

plt.plot(parameters, end_to_end_avg_delays, marker="v", color="g")
plt.xlabel(parameter)
plt.ylabel(metrics[1])
plt.show()

plt.plot(parameters, packet_delivery_ratios, marker="<", color="r")
plt.xlabel(parameter)
plt.ylabel(metrics[2])
plt.show()

plt.plot(parameters, packet_drop_ratios, marker=">", color="y")
plt.xlabel(parameter)
plt.ylabel(metrics[3])
plt.show()