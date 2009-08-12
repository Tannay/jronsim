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

import houseSimulation.HouseIO;
import TranRunJLite.*;
import javax.swing.SwingUtilities;
import userInterface.UserInterfaceJFrame;

/**
 *
 * @author bill
 */
public class ThermostatSys extends TrjSys {

    String name;
    private final ControlTask heaterContTask;
    private final ControlTask coolerContTask;
    private final CoordinatorTask coordinator;
    private final SupervisorTask supervisor;
    private final GoalSeekerTask goalSeeker;
    private final UserInterfaceTask userInterface;

    public ThermostatSys(String name, TrjTime tm, HouseIO therm) {
        super(tm);

        this.name = name;

        heaterContTask = new ControlTask("Heater Control Task", this,
                true, therm, 5.0);
        coolerContTask = new ControlTask("Cooler Control Task", this,
                false, therm, 5.0);

        coordinator = new CoordinatorTask("Coordinator Task",
                this, heaterContTask, coolerContTask, 5.0);
        coordinator.setMode(CoordinatorMode.COOLING);

        supervisor = new SupervisorTask("Supervisor Task", this,
                5.0);

        goalSeeker = new GoalSeekerTask("Goal Seeker Task", this,
                supervisor, coordinator, 5.0);

        userInterface = new UserInterfaceTask("User Interface Task", this,
                goalSeeker, 0.5);
    }

    public ThermostatSys(String name, TrjTime tm, HouseIO therm, boolean uiFlag) {
        super(tm);

        this.name = name;

        heaterContTask = new ControlTask("Heater Control Task", this,
                true, therm, 5.0);
        coolerContTask = new ControlTask("Cooler Control Task", this,
                false, therm, 5.0);

        coordinator = new CoordinatorTask("Coordinator Task",
                this, heaterContTask, coolerContTask, 5.0);
        coordinator.setMode(CoordinatorMode.COOLING);

        supervisor = new SupervisorTask("Supervisor Task", this,
                5.0);

        goalSeeker = new GoalSeekerTask("Goal Seeker Task", this,
                supervisor, coordinator, 5.0);

        userInterface = new UserInterfaceTask("User Interface Task", this,
                goalSeeker, 0.5);

        if (uiFlag) {
            UserInterfaceJFrame gui = new UserInterfaceJFrame(userInterface);
            SwingUtilities.invokeLater(gui);
        }
    }
}
