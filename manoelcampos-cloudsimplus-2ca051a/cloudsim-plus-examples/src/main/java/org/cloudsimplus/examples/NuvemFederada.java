/* * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2018 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.*/


package org.cloudsimplus.examples;

import org.apache.commons.io.FileUtils;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.hosts.network.NetworkHost;
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology;
import org.cloudbus.cloudsim.network.topologies.NetworkTopology;
import org.cloudbus.cloudsim.resources.File;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmGroup;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import javax.sound.sampled.AudioFormat;
import java.beans.Encoder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A minimal but organized, structured and re-usable CloudSim Plus example
 * which shows good coding practices for creating simulation scenarios.
 *
 * <p>It defines a set of constants that enables a developer
 * to change the number of Hosts, VMs and Cloudlets to create
 * and the number of {@link Pe}s for Hosts, VMs and Cloudlets.</p>
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.0*/


public class NuvemFederada {

    private static final int  HOST = 1;
    private static final int  HOST_MIPS = 1000;
    private static final long HOST_BW = 10_000; //in Megabits/s
    private static final int  HOST_PES = 64; //Total de cores por host
    private static final int  HOST_RAM = 131072; //in Megabytes
    private static final long HOST_STORAGE = 10_000_000; //in Megabytes

    private static final int  HOST_PES_COMPUTATION = 64; //Total de cores por host
    private static final int  HOST_RAM_COMPUTATION = 524288; //in Megabytes
    private static final long HOST_STORAGE_COMPUTATION = 1_000_000; //in Megabytes

    private static final int VMS = 20;
    private static final int VM_PES = 64;

    private static final int CLOUDLETS = 4;
    private static final int CLOUDLET_PES = 2;
    private static final int CLOUDLET_LENGTH = 10_000; //MIPS

    private final CloudSim simulation;
    private DatacenterBroker broker;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;

    private Datacenter datacenter_UFRGS;
    private Datacenter datacenter_UFSM;
    private Datacenter datacenter_UFPel;

    private static final HashMap<String, Integer> priorityTypes = new HashMap<String, Integer>(){{
        priorityTypes.put("Small",3);
        priorityTypes.put("Medium",2);
        priorityTypes.put("Large",1);
    }
    };

    private static final HashMap<String, Integer> numberOfCloudletsByType = new HashMap<String, Integer>(){{
        priorityTypes.put("Small",0);
        priorityTypes.put("Medium",0);
        priorityTypes.put("Large",0);
    }
    };

    public static void main(String[] args) {
        new NuvemFederada();
    }

    private NuvemFederada() {

        //DataCenter => Hosts (Maquinas fisicas)
        simulation = new CloudSim();
        List<Datacenter> datacenterList = new ArrayList<>();
        datacenter_UFRGS = createDatacenter();
        datacenter_UFSM = createDatacenter();
        datacenter_UFPel = createDatacenter();

        //Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
        broker = new DatacenterBrokerSimple(simulation);
broker.setDatacenterMapper(new BiFunction<Datacenter, Vm, Datacenter>() {
            @Override
            public Datacenter apply(Datacenter datacenter, Vm vm) {
                Datacenter lessBusy = null;
                long numberOfHostsBusy = 1;
                for (Datacenter center : datacenterList){
                    if (center.getActiveHostsNumber() < numberOfHostsBusy){
                        numberOfHostsBusy = center.getActiveHostsNumber();
                        lessBusy = center;
                    }
                }
            if(lessBusy==null) {
                return datacenterList.get(0);
            }
            return lessBusy;
            }
        });


        vmList = createVms();
        cloudletList = createCloudlets();
        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        simulation.start();

        final List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        new CloudletsTableBuilder(finishedCloudlets).build();
    }


    private Datacenter createDatacenter() {
        final List<Host> hostList = new ArrayList<>(8);
        hostList.add(createNetworkHost());
        for(int i = 0; i < 8; i++) {
            Host host = createHost();
            hostList.add(host);
        }

        //Uses a VmAllocationPolicySimple by default to allocate VMs
        return new DatacenterSimple(simulation, hostList);
    }

    private Host createHost() {
        final List<Pe> peList = new ArrayList<>(64);
        //List of Host's CPUs (Processing Elements, PEs)
        for (int i = 0; i < 64; i++) {
            //Uses a PeProvisionerSimple by default to provision PEs for VMs
            peList.add(new PeSimple(HOST_MIPS));
        }

/*        Uses ResourceProvisionerSimple by default for RAM and BW provisioning
        and VmSchedulerSpaceShared for VM scheduling.*/


        return new HostSimple(524288, HOST_BW, 1_000_000, peList);
    }


    private Host createNetworkHost(){
        final List<Pe> peList = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            //Uses a PeProvisionerSimple by default to provision PEs for VMs
            peList.add(new PeSimple(HOST_MIPS));
        }
        return new NetworkHost(131072,HOST_BW,10_000_000,peList);
    }



    private List<Vm> createVms() {
        final List<Vm> list = new ArrayList<>(VMS);
        for (int i = 0; i < VMS; i++) {
            //Uses a CloudletSchedulerTimeShared by default to schedule Cloudlets
            //Politica que tem que alterar
            final Vm vm = new VmSimple(HOST_MIPS, VM_PES, new
                CloudletScheduler ());
            vm.setRam(512).setBw(1000).setSize(1_000_000);
            list.add(vm);
        }

        return list;
    }

    private void configureNetwork () {
        //Configure network by mapping CloudSim entities
        NetworkTopology networkTopology = new BriteNetworkTopology();
        simulation.setNetworkTopology(networkTopology);
        networkTopology.addLink(broker, datacenter_UFRGS, 10, 1);
    }



    private List<Cloudlet> createCloudlets() {
        final List<Cloudlet> list = new ArrayList<>(CLOUDLETS);

        //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
        final UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/jobsSitioUFPel.txt"));
            String st;
            while ((st = br.readLine()) != null) {
                System.out.println(st);
            }
        }catch(Exception e){
            System.out.println("Error reading file");
        }
        for (String key : numberOfCloudletsByType.keySet()){
            final Cloudlet cloudlet = new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel);
            cloudlet.setSizes(1024);
            cloudlet.setPriority(priorityTypes.get(key));
            list.add(cloudlet);
        }

        return list;
    }
}
