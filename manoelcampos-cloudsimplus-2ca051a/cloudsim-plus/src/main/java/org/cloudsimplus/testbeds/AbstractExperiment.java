/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
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
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.testbeds;

/**
 * A base class to to provide base features for {@link Experiment} and {@link ExperimentRunner}.
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 6.1.0
 */
public abstract class AbstractExperiment implements Runnable {
    protected boolean verbose;

    public AbstractExperiment() {
        this.verbose = false;
    }

    /**
     * Indicates if simulation results of the experiment have to be output.
     * @return
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Prints a line break only if {@link #isVerbose()}.
     */
    public AbstractExperiment println(){
        return println("");
    }

    /**
     * Prints a message and a line break only if {@link #isVerbose()}.
     * @param msg the message to print
     */
    public AbstractExperiment println(final String msg){
        if(verbose){
            System.out.println(msg);
        }

        return this;
    }

    /**
     * Prints a formatted message and a line break only if {@link #isVerbose()}.
     * @param format the message format
     * @param args the values to print
     */
    public AbstractExperiment println(final String format, final Object ...args){
        return print(format + "%n", args);
    }

    /**
     * Prints a message only if {@link #isVerbose()}.
     * @param msg the message to print
     */
    public AbstractExperiment print(final String msg){
        if(verbose){
            System.out.print(msg);
        }

        return this;
    }

    /**
     * Prints a formatted message only if {@link #isVerbose()}.
     * @param format the message format
     * @param args the values to print
     */
    public AbstractExperiment print(final String format, final Object ...args){
        if(verbose){
            System.out.printf(format, args);
        }

        return this;
    }

    /**
     * Indicates if simulation results of the experiment don't have to be
     * output.
     * @return
     */
    public boolean isNotVerbose() {
        return !verbose;
    }

    /**
     * Defines if simulation results of the experiment have to be output or not.
     *
     * @param verbose true if the results have to be output, false otherwise
     * @return
     */
    public AbstractExperiment setVerbose(final boolean verbose) {
        this.verbose = verbose;
        return this;
    }
}
