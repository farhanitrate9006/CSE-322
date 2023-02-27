import matplotlib.pyplot as plt 
graphs = 'graphs-802.11'

# reading statistics from log_1.txt
log_file = open("log_1.txt", "r")
parameter = log_file.readline()
metrics = log_file.readline().split()

parameters = []
network_throughputs = []
end_to_end_avg_delays = []
packet_delivery_ratios = []
packet_drop_ratios = []
energy_consumption = []

for line in log_file:
    if len(line.split()) == 1:
        parameters.append(int(line))
    else:
        metrics_list = line.split()
        network_throughputs.append(float(metrics_list[0]))
        end_to_end_avg_delays.append(float(metrics_list[1]))
        packet_delivery_ratios.append(float(metrics_list[2]))
        packet_drop_ratios.append(float(metrics_list[3]))
        energy_consumption.append(float(metrics_list[4]))

log_file.close()

# reading statistics from log_2.txt
log_file = open("log_2.txt", "r")
parameter = log_file.readline()
metrics = log_file.readline().split()

network_throughputs_2 = []
end_to_end_avg_delays_2 = []
packet_delivery_ratios_2 = []
packet_drop_ratios_2 = []
energy_consumption_2 = []

for line in log_file:
    if len(line.split()) != 1:
        metrics_list = line.split()
        network_throughputs_2.append(float(metrics_list[0]))
        end_to_end_avg_delays_2.append(float(metrics_list[1]))
        packet_delivery_ratios_2.append(float(metrics_list[2]))
        packet_drop_ratios_2.append(float(metrics_list[3]))
        energy_consumption_2.append(float(metrics_list[4]))

log_file.close()

# plotting graphs
plt.plot(parameters, network_throughputs, marker="^", color="b")
plt.plot(parameters, network_throughputs_2, marker="^", color="g")
plt.xlabel(parameter)
plt.ylabel(metrics[0])
plt.savefig(graphs + '/' + parameter.strip() + '/network_throughputs.png')
plt.show()

plt.plot(parameters, end_to_end_avg_delays, marker="^", color="b")
plt.plot(parameters, end_to_end_avg_delays_2, marker="^", color="g")
plt.xlabel(parameter)
plt.ylabel(metrics[1])
plt.savefig(graphs + '/' + parameter.strip() + '/end_to_end_avg_delays.png')
plt.show()

plt.plot(parameters, packet_delivery_ratios, marker="^", color="b")
plt.plot(parameters, packet_delivery_ratios_2, marker="^", color="g")
plt.xlabel(parameter)
plt.ylabel(metrics[2])
plt.savefig(graphs + '/' + parameter.strip() + '/packet_delivery_ratios.png')
plt.show()

plt.plot(parameters, packet_drop_ratios, marker="^", color="b")
plt.plot(parameters, packet_drop_ratios_2, marker="^", color="g")
plt.xlabel(parameter)
plt.ylabel(metrics[3])
plt.savefig(graphs + '/' + parameter.strip() + '/packet_drop_ratios.png')
plt.show()

plt.plot(parameters, energy_consumption, marker="^", color="b")
plt.plot(parameters, energy_consumption_2, marker="^", color="g")
plt.xlabel(parameter)
plt.ylabel(metrics[4])
plt.savefig(graphs + '/' + parameter.strip() + '/energy_consumption.png')
plt.show()