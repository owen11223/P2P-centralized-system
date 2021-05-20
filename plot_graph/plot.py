import matplotlib.pyplot as plt
import numpy as np


def data():
    weaklist_onenode = []
    weaklist_twonode = []
    weaklist_fournode = []
    stronglist_twonode = []
    stronglist_fournode = []
    stronglist_mediumtwonode = []
    stronglist_mediumfournode = []
    non_list = []

    with open("allweak.txt", "r") as f:
        for line in f:
            val = line.strip().split(' ')
            if val[4] == 'weak1':
                weaklist_onenode.append(10000 / (float(val[3]) * 0.001))
            elif val[4] == 'weak2':
                weaklist_twonode.append(10000 / (float(val[3]) * 0.001))
            elif val[4] == 'weak4':
                weaklist_fournode.append(10000 / (float(val[3]) * 0.001))
            elif val[4] == 'strong_small2':
                stronglist_twonode.append(100 / (float(val[3]) * 0.001))  # mb/s
            elif val[4] == 'strong_small4':
                stronglist_fournode.append(100 / (float(val[3]) * 0.001))
            elif val[4] == 'strong_medium2':
                stronglist_mediumtwonode.append(100 / (float(val[3]) * 0.001))  # mb/s
            elif val[4] == 'strong_medium4':
                stronglist_mediumfournode.append(100 / (float(val[3]) * 0.001))
            else:
                non_list.append(int(val[3]))
    #print("one node: ", weaklist_onenode)
    #print("two node: ", weaklist_twonode)
    #print("four node: ", weaklist_fournode)
    #print("not show: ", non_list)
    return weaklist_onenode, weaklist_twonode, weaklist_fournode, stronglist_twonode, stronglist_fournode,\
           stronglist_mediumtwonode,stronglist_mediumfournode


def weak(weaklist_onenode, weaklist_twonode, weaklist_fournode):
    mean_weak_onenode = np.array(weaklist_onenode).mean()
    print("mean for weak test with one ", mean_weak_onenode)
    std_weak_onenode = np.array(weaklist_onenode).std()
    print("std for weak test with one ", std_weak_onenode)
    mean_weak_twonode = np.array(weaklist_twonode).mean()
    print("mean for weak test with two ", mean_weak_twonode)
    std_weak_twonode = np.array(weaklist_twonode).std()
    print("std for weak test with two ", std_weak_twonode)
    mean_weak_fournode = np.array(weaklist_fournode).mean()
    print("mean for weak test with four ", mean_weak_fournode)
    std_weak_fournode = np.array(weaklist_fournode).std()
    print("std for weak test with four ", std_weak_fournode)

    labels = ['1_node', '2_node', '4_node']
    x_pos = np.arange(len(labels))
    CTEs = [mean_weak_onenode, mean_weak_twonode, mean_weak_fournode]
    error = [std_weak_onenode, std_weak_twonode, std_weak_fournode]

    fig, ax = plt.subplots()
    ax.bar(x_pos, CTEs,
           yerr=error,
           align='center',
           alpha=0.5,
           ecolor='black',
           capsize=10)
    ax.set_ylabel('throughput (number job per second)')
    ax.set_xticks(x_pos)
    ax.set_xticklabels(labels)
    ax.set_title('weak scaling')
    ax.yaxis.grid(True)
    plt.tight_layout()
    plt.show()


def stong_samll(stronglist_twonode, stronglist_fournode):
    mean_strong_twonode = np.array(stronglist_twonode).mean()
    std_strong_twonode = np.array(stronglist_twonode).std()
    mean_strong_fournode = np.array(stronglist_fournode).mean()
    std_strong_fournode = np.array(stronglist_fournode).std()

    print("\n\n\nmean for strong test with small files and two ", mean_strong_twonode)
    print("std for strong test with small files and two ", std_strong_twonode)
    print("mean for strong test with small files and four ", mean_strong_fournode)
    print("std for strong test with small files and four ", std_strong_fournode)

    labels = ['2_node', '4_node']
    x_pos = np.arange(len(labels))
    CTEs = [mean_strong_twonode, mean_strong_fournode]
    error = [std_strong_twonode, std_strong_fournode]

    fig, ax = plt.subplots()
    ax.bar(x_pos, CTEs,
           yerr=error,
           align='center',
           alpha=0.5,
           ecolor='black',
           capsize=10)
    ax.set_ylabel('throughput (number bites per second)')
    ax.set_xticks(x_pos)
    ax.set_xticklabels(labels)
    ax.set_title('strong scaling for small data sets')
    ax.yaxis.grid(True)
    plt.tight_layout()
    plt.show()


def strong_medium(stronglist_mediumtwonode,stronglist_mediumfournode):
    mean_strong_mediumtwonode = np.array(stronglist_mediumtwonode).mean()
    std_strong_mediumtwonode = np.array(stronglist_mediumtwonode).std()
    mean_strong_mediumfournode = np.array(stronglist_mediumfournode).mean()
    std_strong_mediumfournode = np.array(stronglist_mediumfournode).std()

    print("\n\n\nmean for strong test with medium files and two ", mean_strong_mediumtwonode)
    print("std for strong test with medium files and two ", std_strong_mediumtwonode)
    print("mean for strong test with medium files and four ", mean_strong_mediumfournode)
    print("std for strong test with medium files and four ", std_strong_mediumfournode)

    labels = ['2_node', '4_node']
    x_pos = np.arange(len(labels))
    CTEs = [mean_strong_mediumtwonode, mean_strong_mediumfournode]
    error = [std_strong_mediumtwonode, std_strong_mediumfournode]

    fig, ax = plt.subplots()
    ax.bar(x_pos, CTEs,
           yerr=error,
           align='center',
           alpha=0.5,
           ecolor='black',
           capsize=10)
    ax.set_ylabel('throughput (number MB per second)')
    ax.set_xticks(x_pos)
    ax.set_xticklabels(labels)
    ax.set_title('strong scaling for medium data sets')
    ax.yaxis.grid(True)
    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    weaklist_onenode, weaklist_twonode, weaklist_fournode, stronglist_twonode, stronglist_fournode, stronglist_mediumtwonode, stronglist_mediumfournode = data()
    weak(weaklist_onenode, weaklist_twonode, weaklist_fournode)
    stong_samll(stronglist_twonode, stronglist_fournode)
    strong_medium(stronglist_mediumtwonode, stronglist_mediumfournode)
