/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudsimplus.examples;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerAbstract;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;

import java.util.List;


public class CloudletSchedulerCrescentPriority extends CloudletSchedulerAbstract {

    /**
     * {@inheritDoc}
     *
     * <p>
     * <b>For this scheduler, this list is always empty, once the VM PEs
     * are shared across all Cloudlets running inside a VM. Each Cloudlet has
     * the opportunity to use the PEs for a given time-slice.</b></p>
     *
     * @return {@inheritDoc}
     */
    @Override
    public List<CloudletExecution> getCloudletWaitingList() {
        //The method was overridden here just to extend its JavaDoc.
        return super.getCloudletWaitingList();
    }

    /**
     * Moves a Cloudlet that was paused and has just been resumed to the
     * Cloudlet execution list.
     *
     * @param cloudlet the Cloudlet to move from the paused to the exec lit
     * @return the Cloudlet expected finish time
     */
    private double movePausedCloudletToExecListAndGetExpectedFinishTime(final CloudletExecution cloudlet) {
        getCloudletPausedList().remove(cloudlet);
        addCloudletToExecList(cloudlet);
        return cloudletEstimatedFinishTime(cloudlet, getVm().getSimulation().clock());
    }

    @Override
    public double cloudletResume(final Cloudlet cloudlet) {
        return findCloudletInList(cloudlet, getCloudletPausedList())
                .map(this::movePausedCloudletToExecListAndGetExpectedFinishTime)
                .orElse(0.0);
    }

    /**
     * This time-shared scheduler shares the CPU time between all executing
     * cloudlets, giving the same CPU time-slice for each Cloudlet to execute. It
     * always allow any submitted Cloudlets to be immediately added to the
     * execution list. By this way, it doesn't matter what Cloudlet is being
     * submitted, since it will always include it in the execution list.
     *
     * @param cloudlet the Cloudlet that will be added to the execution list.
     * @return always <b>true</b> to indicate that any submitted Cloudlet can be
     * immediately added to the execution list
     */
    @Override
    protected boolean canExecuteCloudletInternal(final CloudletExecution cloudlet) {
        return true;
    }

}
