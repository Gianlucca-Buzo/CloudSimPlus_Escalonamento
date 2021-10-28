/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.examples.network;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology;
import org.cloudbus.cloudsim.network.topologies.NetworkTopology;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple example showing how to create 2 datacenters with 1 host each and run
 * cloudlets of 2 users. It also sets a network topology.
 */
public class NetworkCloud {
    private static final int VM_PES = 2;

    private static final int  HOST = 2;
    private static final int  HOST_MIPS = 1000;
    private static final long HOST_BW = 10_000;

    private final List<Datacenter> datacenterList;
    private final List<DatacenterBroker> brokerList;

    private final List<Cloudlet> cloudletList1;
    private final List<Cloudlet> cloudletList2;
    private final List<Vm> vmlist1;
    private final List<Vm> vmlist2;
    private final CloudSim simulation;

    public static void main(String[] args) {
        new NetworkCloud();
    }

    private NetworkCloud() {
        System.out.println("Starting " + getClass().getSimpleName());

        datacenterList = new ArrayList<>();
        brokerList = new ArrayList<>();
        vmlist1 = new ArrayList<>();
        vmlist2 = new ArrayList<>();

        simulation = new CloudSim();

        for (int i = 0; i < 3; i++) {
            datacenterList.add(createDatacenter());
        }

        for (int i = 0; i < 3; i++) {
            brokerList.add(createBroker(i));
        }

        createNetwork();
        createAndSubmitVms();

        cloudletList1 = new ArrayList<>();
        cloudletList2 = new ArrayList<>();
        createAndSubmitCloudlets();

        simulation.start();

        for (DatacenterBroker broker : brokerList) {
            printFinishedCloudletList(broker);
        }
        System.out.println(getClass().getSimpleName() + " finished!");
    }

    private void printFinishedCloudletList(DatacenterBroker broker) {
        new CloudletsTableBuilder(broker.getCloudletFinishedList())
                .setTitle("Broker " + broker)
                .build();
    }

    private void createAndSubmitCloudlets() {
        final long length = 40000;
        final long fileSize = 300;
        final long outputSize = 300;
        final UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet cloudlet1 =
            new CloudletSimple(length, VM_PES)
                .setFileSize(fileSize)
                .setOutputSize(outputSize)
                .setUtilizationModel(utilizationModel);

        Cloudlet cloudlet2 =
            new CloudletSimple(length, VM_PES)
                .setFileSize(fileSize)
                .setOutputSize(outputSize)
                .setUtilizationModel(utilizationModel);

        cloudletList1.add(cloudlet1);
        cloudletList2.add(cloudlet2);

        brokerList.get(0).submitCloudletList(cloudletList1);
        brokerList.get(1).submitCloudletList(cloudletList2);
    }

    private void createAndSubmitVms() {
        final long size = 10000; //image size (Megabyte)
        final int mips = 250;
        final int ram = 512; //vm memory (Megabyte)
        final long bw = 1000;

        Vm vm1 = new VmSimple(mips, VM_PES)
                .setRam(ram).setBw(bw).setSize(size);

        Vm vm2 = new VmSimple(mips, VM_PES)
                .setRam(ram).setBw(bw).setSize(size);

        vmlist1.add(vm1);
        vmlist2.add(vm2);

        brokerList.get(0).submitVmList(vmlist1);
        brokerList.get(1).submitVmList(vmlist2);
    }

    private void createNetwork() {
        //load the network topology file
        NetworkTopology networkTopology = new BriteNetworkTopology();
        simulation.setNetworkTopology(networkTopology);
    }

    private Datacenter createDatacenter() {
        List<Host> hostList = new ArrayList<>(HOST);
        Host controllerHost = createHost(16,131072,10_000_000);
        hostList.add(controllerHost);
        Host computationHost = createHost(64,524288,1_000_000);
        hostList.add(computationHost);
        return new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
    }

    private Host createHost(int host_pes, int host_ram, int host_storage) {
        final List<Pe> peList = new ArrayList<>(host_pes);
        for (int i = 0; i < host_pes; i++) {
            peList.add(new PeSimple(HOST_MIPS));
        }
        return new HostSimple(host_ram, HOST_BW, host_storage, peList);
    }

    private DatacenterBroker createBroker(int id) {
        return new DatacenterBrokerSimple(simulation);
    }

}
