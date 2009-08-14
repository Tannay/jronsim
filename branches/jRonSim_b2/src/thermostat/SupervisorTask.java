/*
 * Copyright (c) 2009, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package thermostat;

import TranRunJLite.*;

/**The Supervisor Task stores and returns setpoint information.
 *
 * @author William Burke <billstron@gmail.com>
 */
public class SupervisorTask extends TrjTask {

    private double dt;
    private double tNext;
    private SupervisorMode mode;
    private SetpointTable table;
    private double Tsp;
    private double TspTable;
    private boolean newSp;

    /** Constructs the supervisor task.
     * 
     * @param name
     * @param sys
     * @param dt
     */
    public SupervisorTask(String name, TrjSys sys,
            double dt) {
        super(name, sys, 0 /*Initial State*/, true /*Start Active*/);

        stateNames.add("Hold State");
        stateNames.add("Tables State");

        this.table = new SetpointTable();
        this.TspTable = table.getTsp(sys.GetCalendar());
        this.Tsp = this.TspTable;
        this.newSp = true;
        this.mode = SupervisorMode.TABLES;
        this.dt = dt;
        this.tNext = 0;
    }
    private final int sHold = 0;
    private final int sTables = 1;

    /** Indicates if the current setpoint is new.
     * 
     * @return
     */
    public boolean isNewSetpoint() {
        return this.newSp;
    }

    /** Returns the current setpoint.  If the setpoint is new, the flag is reset
     * as well.
     * 
     * @return
     */
    public double getSetpoint() {
        this.newSp = false;
        return this.Tsp;
    }

    /** Tells if the hold state is currently active.
     * 
     * @return
     */
    public boolean isHoldOn() {
        boolean flag = false;
        if (mode == SupervisorMode.HOLD) {
            flag = true;
        }
        return flag;
    }

    /** Sets the Supervisor into the hold state.
     * 
     * @param holdOn
     */
    public void setHoldOn(boolean holdOn) {
        if (holdOn) {
            mode = SupervisorMode.HOLD;
        } else {
            mode = SupervisorMode.TABLES;
        }
    }

    /** Runs the Supervisor task.
     * 
     * @param sys
     * @return
     */
    @Override
    public boolean RunTask(TrjSys sys) {
        if (sys.GetRunningTime() >= tNext) {
            //System.out.println("supervisor state: " + this.currentState);
            switch (this.currentState) {
                case sHold:  // no setpoint changes.
                    // Upon entry, don't indicate a new setpoint
                    if (this.runEntry) {
                        //System.out.println("SupervisorState: sHold");
                        //newSp = false;
                    }
                    // Determine the state transition based on the mode variable
                    this.nextState = -1;
                    if (mode != SupervisorMode.HOLD) {
                        this.nextState = sTables;
                    }
                    break;
                case sTables:  // changes based on the setpoint tables
                    // Upon entry, always indicate a new setpoint
                    if (this.runEntry) {
                        //System.out.println("SupervisorState: sTables");
                        //newSp = true;
                    }
                    // Get the new setpoint and compare it to the old one.
                    // if the setpoint changed, indicate as such.
                    TspTable = table.getTsp(sys.GetCalendar());
                    if (Tsp != TspTable) {
                        newSp = true;
                        Tsp = TspTable;
                    }
                    // determine the state transtion based on the mode flag and
                    // the override flag
                    this.nextState = -1;
                    if (mode != SupervisorMode.TABLES) {
                        this.nextState = sHold;
                    }
                    break;
            } // case
            // update the timing variable
            tNext += dt;
        } else {
            if (runEntry) {
                nextState = currentState;
            }
        }
        return false;
    }
}
