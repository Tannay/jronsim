/*
 * Copyright (c) 2010, Regents of the University of California
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
package house.occupant;

import java.util.Calendar;

import util.BoundedRand;
import util.Conversion;

import TranRunJLite.TrjSys;
import TranRunJLite.TrjTask;

/**
 * @author William Burke <billstron@gmail.com>
 * @date Apr 16, 2010
 */
public class OccupantTask extends TrjTask {

	private int resNum; // the resident identifier.
	private double insideTemp; // inside temperature.
	private double setpointTemp; // current house setpoint
	private double changeTemp; // amount to modify temperature by
	private double curMotivation; // the current motivation probability.
	private double delMotivation; // ammount to increase motivatoin
	private double transTime;
	private int transNext;
	private OccupantParams prefs;
	private double tNext;
	private BoundedRand rand;
	private LivingSpaceSys sys;

	// Control Task states
	static final int AWAKE_COMFORTABLE_STATE = 0;
	static final int AWAKE_WARM_STATE = 1;
	static final int AWAKE_HOT_STATE = 2;
	static final int AWAKE_COOL_STATE = 3;
	static final int AWAKE_COLD_STATE = 4;
	static final int SLEEPING_STATE = 5;
	static final int AWAY_STATE = 6;

	/**
	 * @param name
	 * @param sys
	 * @param initialState
	 * @param taskActive
	 */
	public OccupantTask(String name, LivingSpaceSys sys, OccupantParams prefs,
			int resNum, BoundedRand rand) {
		super(name, sys, AWAKE_COMFORTABLE_STATE /* initial state */, true /* initially active */);

		this.prefs = prefs;
		this.resNum = resNum;
		this.rand = rand;
		sys.specifyHome();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TranRunJLite.TrjTask#RunTask(TranRunJLite.TrjSys)
	 */
	@Override
	public boolean RunTask(TrjSys sys) {
		double t = sys.GetRunningTime();
		switch (this.currentState) {
		case AWAKE_COMFORTABLE_STATE:
			this.AwakeComfortableState(t);
			break;
		case AWAKE_WARM_STATE:
			break;
		case AWAKE_HOT_STATE:
			break;
		case AWAKE_COOL_STATE:
			break;
		case AWAKE_COLD_STATE:
			break;
		case SLEEPING_STATE:
			break;
		case AWAY_STATE:
			break;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TranRunJLite.TrjTask#RunTaskNow(TranRunJLite.TrjSys)
	 */
	@Override
	public boolean RunTaskNow(TrjSys sys) {

		return CheckTime(sys.GetRunningTime());
	}

	private int AwakeComfortableState(double t) {
		// Get the current calendar
		//System.out.println(t);
		Calendar now = this.sys.GetClaendar(t);
		if (this.runEntry) {
			double tHrs = Conversion.CalendarToHourOfDay(now);

			// prepare the time based state transition variables.
			transTime = Double.POSITIVE_INFINITY;
			transNext = -1;
			// get the current level of motivation.
			curMotivation = 0;
			// specify to the room task that the resident is home
			sys.specifyHome();
			sys.specifyHome();
		}

		double discomfortDelta = 0;

		// Get the inside temperature and setpoint
		insideTemp = sys.getTempInside();
		setpointTemp = sys.getSetpointTemp();

		int nextState = -1; // Default is no transition
		double transHour = 0;

		// for checking the transition times.
		double tHrs = Conversion.CalendarToHourOfDay(now);
		double leaveMod = 0;
		double arriveMod = 0;
		if (prefs.arriveTime[0] < prefs.leaveTime[0]) {
			if (tHrs > prefs.leaveTime[0])
				arriveMod = 24;
			else
				leaveMod = -24;
		}

		double sleepMod = 0;
		double wakeMod = 0;
		if (prefs.wakeTime[0] < prefs.sleepTime[0]) {
			if (tHrs > prefs.sleepTime[0])
				wakeMod = 24;
			else
				sleepMod = -24;
		}

		// if the transition time hasn't been set yet
		// First set if based on the leave time if past the start of window
		// then set it based on sleep if past the start of window.
		if (transTime == Double.POSITIVE_INFINITY) {
			if (tHrs > (prefs.leaveTime[0] + leaveMod)
					&& tHrs < (prefs.arriveTime[0] + arriveMod)) {
				transHour = rand.getBoundedRand(prefs.leaveTime[0],
						prefs.leaveTime[0] + prefs.leaveTime[1])
						+ leaveMod;
				transNext = AWAY_STATE;
				transTime = t + (transHour - tHrs) * 3600;
			} else if (tHrs > (prefs.sleepTime[0] + sleepMod)
					&& tHrs < (prefs.wakeTime[0] + wakeMod)) {
				transHour = rand.getBoundedRand(prefs.sleepTime[0],
						prefs.sleepTime[0] + prefs.sleepTime[1])
						+ sleepMod;
				transNext = SLEEPING_STATE;
				transTime = t + (transHour - tHrs) * 3600;
			}
		}

		// set the time based transition when past time.
		if (t > transTime)
			nextState = transNext;
		// if the next transition is to the sleeping state, check the comfort
		if (nextState == -1 || nextState == SLEEPING_STATE) {
			if (insideTemp >= prefs.comfortTemp[OccupantParams.WARM])
				nextState = AWAKE_WARM_STATE;
			else if (insideTemp <= prefs.comfortTemp[OccupantParams.COOL])
				nextState = AWAKE_COOL_STATE;
		}
		return nextState;
	}

	private int AwakeWarmState(double t) {
		// Get the current calendar
		Calendar now = this.sys.GetClaendar(t);

		if (this.runEntry) {
			double tHrs = Conversion.CalendarToHourOfDay(now);
			// prepare the time based state transition variables.
			transTime = Double.POSITIVE_INFINITY;
			transNext = -1;
			// get the current level of motivation.
			curMotivation = prefs.motivationProb[OccupantParams.WARM];
		}

		double randNum = 0;

		// Get the inside temperature and setpoint
		insideTemp = sys.getTempInside();
		setpointTemp = sys.getSetpointTemp();

		// Lower the setpoint if the person is warm and the timer has expired.
		if (t > tNext && setpointTemp > prefs.comfortTemp[OccupantParams.WARM]) {
			// Flip a coin
			randNum = rand.getBoundedRand(0, 1);
			// Check if we are in a DR event. Get the proper probs.
			if (sys.getDRState() > 0) {
				// if in a DR event, check the DR motivation prob
				if (randNum < prefs.DRmotivationProb[OccupantParams.WARM]) {
					changeTemp = -Math.ceil(setpointTemp
							- prefs.DRcomfortTemp[OccupantParams.COMFORT]);
					// reset the motivation
					curMotivation = prefs.motivationProb[OccupantParams.WARM];
				}
			} else {
				// if not in a DR event, check the current motivation
				if (randNum < curMotivation) {
					changeTemp = -Math.ceil(setpointTemp
							- prefs.comfortTemp[OccupantParams.COMFORT]);
					// update the motivation
					curMotivation += delMotivation;
				}
			}
			// update the next check time
			tNext = t + prefs.tPatience;
		}

		// Make any setpoint adjustments that haven't already been made
		// Adjust the setpoint one press at a time.
		/*
		 * if( changeTemp > 0 ) { UserInterfaceArrowUp( task ); changeTemp -= 1;
		 * } if( changeTemp < 0 ) { UserInterfaceArrowDown( task ); changeTemp
		 * += 1; }
		 */
		if (changeTemp != 0) {
			sys.adjustSetpoint(changeTemp);
			sys.adjustSetpoint(changeTemp);
			changeTemp = 0;
		}

		int nextState = -1; // Default is no transition
		double transHour = 0;

		// for checking the transition times.
		double tHrs = Conversion.CalendarToHourOfDay(now);
		double leaveMod = 0;
		double arriveMod = 0;
		if (prefs.arriveTime[0] < prefs.leaveTime[0]) {
			if (tHrs > prefs.leaveTime[0])
				arriveMod = 24;
			else
				leaveMod = -24;
		}

		// if the transition time hasn't been set yet
		// First set if based on the leave time if past the start of window
		// then set it based on sleep if past the start of window.
		if (transTime == Double.POSITIVE_INFINITY) {
			if (tHrs > (prefs.leaveTime[0] + leaveMod)
					&& tHrs < (prefs.arriveTime[0] + arriveMod)) {
				transHour = rand.getBoundedRand(prefs.leaveTime[0],
						prefs.leaveTime[0] + prefs.leaveTime[1])
						+ leaveMod;
				transNext = AWAY_STATE;
				transTime = t + (transHour - tHrs) * 3600;
			}
		}

		// transition based on temp.
		if (insideTemp > prefs.comfortTemp[OccupantParams.HOT])
			nextState = AWAKE_HOT_STATE;
		else if (insideTemp <= prefs.comfortTemp[OccupantParams.WARM])
			nextState = AWAKE_COMFORTABLE_STATE;
		// set the time based transition when past time.
		if (t > transTime)
			nextState = transNext;
		return nextState;
	}

	private int AwakeHotState(double t) {
		// Get the current calendar
		Calendar now = this.sys.GetClaendar(t);

		if (this.runEntry) {
			double tHrs = Conversion.CalendarToHourOfDay(now);
			// prepare the time based state transition variables.
			transTime = Double.POSITIVE_INFINITY;
			transNext = -1;
			// get the current level of motivation.
			curMotivation = prefs.motivationProb[OccupantParams.HOT];
		}

		double randNum = 0;

		// Get the inside temperature and setpoint
		insideTemp = sys.getTempInside();
		setpointTemp = sys.getSetpointTemp();

		// Lower the setpoint if the person is hot.
		if (t > tNext && setpointTemp > prefs.comfortTemp[OccupantParams.HOT]) {
			// flip a coin
			randNum = rand.getBoundedRand(0, 1);
			// Check if we are in a DR event. Get the proper probs.
			if (sys.getDRState() > 0) {
				// if in a DR event, check the DR motivation prob
				if (randNum < prefs.DRmotivationProb[OccupantParams.HOT]) {
					changeTemp = -Math.ceil(setpointTemp
							- prefs.DRcomfortTemp[OccupantParams.WARM]);
					// reset the motivation
					curMotivation = prefs.motivationProb[OccupantParams.HOT];
				}
			} else {
				// if not in an event, use the current motivation
				if (randNum < curMotivation) {
					changeTemp = -Math.ceil(setpointTemp
							- prefs.comfortTemp[OccupantParams.WARM]);
					// update the motivation
					curMotivation += delMotivation;
				}
			}
			// update the next time
			tNext = t + prefs.tPatience;
		}

		// Make any setpoint adjustments that haven't already been made
		// Adjust the setpoint one press at a time.
		/*
		 * if( changeTemp > 0 ) { UserInterfaceArrowUp( task ); changeTemp -= 1;
		 * } if( changeTemp < 0 ) { UserInterfaceArrowDown( task ); changeTemp
		 * += 1; }
		 */
		if (changeTemp != 0) {
			sys.adjustSetpoint(changeTemp);
			changeTemp = 0;
		}

		int nextState = -1; // Default is no transition
		double transHour = 0;

		// for checking the transition times.
		double tHrs = Conversion.CalendarToHourOfDay(now);
		double leaveMod = 0;
		double arriveMod = 0;
		if (prefs.arriveTime[0] < prefs.leaveTime[0]) {
			if (tHrs > prefs.leaveTime[0])
				arriveMod = 24;
			else
				leaveMod = -24;
		}

		// if the transition time hasn't been set yet
		// First set if based on the leave time if past the start of window
		// then set it based on sleep if past the start of window.
		if (transTime == Double.POSITIVE_INFINITY) {
			if (tHrs > (prefs.leaveTime[0] + leaveMod)
					&& tHrs < (prefs.arriveTime[0] + arriveMod)) {
				transHour = rand.getBoundedRand(prefs.leaveTime[0],
						prefs.leaveTime[0] + prefs.leaveTime[1])
						+ leaveMod;
				transNext = AWAY_STATE;
				transTime = t + (transHour - tHrs) * 3600;
			}
		}

		// transition based on comfort
		if (insideTemp <= prefs.comfortTemp[OccupantParams.HOT])
			nextState = AWAKE_WARM_STATE;
		// set the time based transition when past time.
		if (t > transTime)
			nextState = transNext;
		return nextState;
	}

	private int AwakeCoolState(double t) {
		// Get the current calendar
		Calendar now = this.sys.GetClaendar(t);

		if (this.runEntry) {
			double tHrs = Conversion.CalendarToHourOfDay(now);

			// prepare the time based state transition variables.
			transTime = Double.POSITIVE_INFINITY;
			transNext = -1;
			// get the current level of motivation.
			curMotivation = prefs.motivationProb[OccupantParams.COOL];
		}

		double randNum = 0;

		// Get the inside temperature and setpoint
		insideTemp = sys.getTempInside();
		setpointTemp = sys.getSetpointTemp();

		// Raise the setpoint if the person is cool
		if (t > tNext && setpointTemp < prefs.comfortTemp[OccupantParams.COOL]) {
			// flip a coin
			randNum = rand.getBoundedRand(0, 1);
			// Check if we are in a DR event. Get the proper probs.
			if (sys.getDRState() > 0) {
				// if in a DR event, check the DR motivation prob
				if (randNum < prefs.DRmotivationProb[OccupantParams.COOL]) {
					changeTemp = -Math.ceil(setpointTemp
							- prefs.DRcomfortTemp[OccupantParams.COMFORT]);
					// reset the motivation
					curMotivation = prefs.motivationProb[OccupantParams.COOL];
				}
			} else {
				if (randNum < curMotivation) {
					changeTemp = Math.ceil(prefs.comfortTemp[OccupantParams.COMFORT]
							- insideTemp);
					// update the motivation
					curMotivation += delMotivation;
				}
			}
			// update the next time
			tNext = t + prefs.tPatience;
		}

		// Make any setpoint adjustments that haven't already been made
		// Adjust the setpoint one press at a time.
		/*
		 * if( changeTemp > 0 ) { UserInterfaceArrowUp( task ); changeTemp -= 1;
		 * } if( changeTemp < 0 ) { UserInterfaceArrowDown( task ); changeTemp
		 * += 1; }
		 */
		if (changeTemp != 0) {
			sys.adjustSetpoint(changeTemp);
			changeTemp = 0;
		}

		int nextState = -1; // Default is no transition
		double transHour = 0;

		// for checking the transition times.
		double tHrs = Conversion.CalendarToHourOfDay(now);
		double leaveMod = 0;
		double arriveMod = 0;
		if (prefs.arriveTime[0] < prefs.leaveTime[0]) {
			if (tHrs > prefs.leaveTime[0])
				arriveMod = 24;
			else
				leaveMod = -24;
		}

		// if the transition time hasn't been set yet
		// First set if based on the leave time if past the start of window
		// then set it based on sleep if past the start of window.
		if (transTime == Double.POSITIVE_INFINITY) {
			if (tHrs > (prefs.leaveTime[0] + leaveMod)
					&& tHrs < (prefs.arriveTime[0] + arriveMod)) {
				transHour = rand.getBoundedRand(prefs.leaveTime[0],
						prefs.leaveTime[0] + prefs.leaveTime[1])
						+ leaveMod;
				transNext = AWAY_STATE;
				transTime = t + (transHour - tHrs) * 3600;
			}
		}

		// comfort based transiton
		if (insideTemp <= prefs.comfortTemp[OccupantParams.COLD])
			nextState = AWAKE_COLD_STATE;
		else if (insideTemp >= prefs.comfortTemp[OccupantParams.COOL])
			nextState = AWAKE_COMFORTABLE_STATE;
		// set the time based transition when past time.
		if (t > transTime)
			nextState = transNext;
		return nextState;
	}

	private int AwakeColdState(double t) {
		// Get the current calendar
		Calendar now = this.sys.GetClaendar(t);

		if (this.runEntry) {
			double tHrs = Conversion.CalendarToHourOfDay(now);
			// prepare the time based state transition variables.
			transTime = Double.POSITIVE_INFINITY;
			transNext = -1;
			// get the current level of motivation.
			curMotivation = prefs.motivationProb[OccupantParams.COLD];
		}

		double randNum = 0;

		// Get the inside temperature and setpoint
		insideTemp = sys.getTempInside();
		setpointTemp = sys.getSetpointTemp();

		// Raise the setpoint if the person is cold
		if (t > tNext && setpointTemp < prefs.comfortTemp[OccupantParams.COLD]) {
			randNum = rand.getBoundedRand(0, 1);
			// Check if we are in a DR event. Get the proper probs.
			if (sys.getDRState() > 0) {
				// if in a DR event, check the DR motivation prob
				if (randNum < prefs.DRmotivationProb[OccupantParams.COLD]) {
					changeTemp = -Math.ceil(setpointTemp
							- prefs.DRcomfortTemp[OccupantParams.COOL]);
					// reset the motivation
					curMotivation = prefs.motivationProb[OccupantParams.COLD];
				}
			} else {
				// if not in an event, use the current motivation
				if (randNum < curMotivation) {
					changeTemp = Math
							.ceil(prefs.comfortTemp[OccupantParams.COOL] - insideTemp);
					// update the motivation
					curMotivation += delMotivation;
				}
			}
			// update the next time
			tNext = t + prefs.tPatience;
		}

		// Make any setpoint adjustments that haven't already been made
		// Adjust the setpoint one press at a time.
		/*
		 * if( changeTemp > 0 ) { UserInterfaceArrowUp( task ); changeTemp -= 1;
		 * } if( changeTemp < 0 ) { UserInterfaceArrowDown( task ); changeTemp
		 * += 1; }
		 */
		if (changeTemp != 0) {
			sys.adjustSetpoint(changeTemp);
			changeTemp = 0;
		}

		int nextState = -1; // Default is no transition
		double transHour = 0;

		// for checking the transition times.
		double tHrs = Conversion.CalendarToHourOfDay(now);
		double leaveMod = 0;
		double arriveMod = 0;
		if (prefs.arriveTime[0] < prefs.leaveTime[0]) {
			if (tHrs > prefs.leaveTime[0])
				arriveMod = 24;
			else
				leaveMod = -24;
		}

		// if the transition time hasn't been set yet
		// First set if based on the leave time if past the start of window
		// then set it based on sleep if past the start of window.
		if (transTime == Double.POSITIVE_INFINITY) {
			if (tHrs > (prefs.leaveTime[0] + leaveMod)
					&& tHrs < (prefs.arriveTime[0] + arriveMod)) {
				transHour = rand.getBoundedRand(prefs.leaveTime[0],
						prefs.leaveTime[0] + prefs.leaveTime[1])
						+ leaveMod;
				transNext = AWAY_STATE;
				transTime = t + (transHour - tHrs) * 3600;
			}
		}

		// comfort based transtions
		if (insideTemp >= prefs.comfortTemp[OccupantParams.COLD])
			nextState = AWAKE_COOL_STATE;
		// set the time based transition when past time.
		if (t > transTime)
			nextState = transNext;
		return nextState;
	}

	private int SleepingState(double t) {
		// Get the current calendar
		Calendar now = this.sys.GetClaendar(t);

		if (this.runEntry) {
			double tHrs = Conversion.CalendarToHourOfDay(now);
			double randNum = 0;

			// prepare the time based state transition variables.
			transTime = Double.POSITIVE_INFINITY;
			transNext = -1;
			// get the current level of motivation.
			curMotivation = prefs.motivationProb[OccupantParams.SLEEPING];

			// get the setpoint
			setpointTemp = sys.getSetpointTemp();

			randNum = rand.getBoundedRand(0, 1);
			if (randNum < prefs.motivationProb[OccupantParams.SLEEPING])
				changeTemp = Math.ceil(prefs.comfortTemp[OccupantParams.SLEEPING]
						- setpointTemp);
			// specify to the room task that the resident is home
			sys.specifyHome();
		}

		// Get the inside temperature and setpoint
		insideTemp = sys.getTempInside();
		setpointTemp = sys.getSetpointTemp();

		// Make any setpoint adjustments that haven't already been made
		// Adjust the setpoint one press at a time.
		/*
		 * if( changeTemp > 0 ) { UserInterfaceArrowUp( task ); changeTemp -= 1;
		 * } if( changeTemp < 0 ) { UserInterfaceArrowDown( task ); changeTemp
		 * += 1; }
		 */
		if (changeTemp != 0) {
			sys.adjustSetpoint(changeTemp);
			changeTemp = 0;
		}

		int nextState = -1; // Default is no transition
		double transHour = 0;

		// for checking the transition times.
		double tHrs = Conversion.CalendarToHourOfDay(now);
		double sleepMod = 0;
		double wakeMod = 0;
		if (prefs.sleepTime[0] < prefs.wakeTime[0]) {
			if (tHrs > prefs.wakeTime[0])
				sleepMod = 24;
			else
				wakeMod = -24;
		}

		// time based transition
		if (transTime == Double.POSITIVE_INFINITY
				&& tHrs > (prefs.wakeTime[0] + wakeMod)
				&& tHrs < (prefs.sleepTime[0] + sleepMod)) {
			transHour = rand.getBoundedRand(prefs.wakeTime[0],
					prefs.wakeTime[0] + prefs.wakeTime[1])
					+ wakeMod;
			transNext = AWAKE_COMFORTABLE_STATE;
			transTime = t + (transHour - tHrs) * 3600;
		}

		// set the time based transition when past time.
		if (t > transTime)
			nextState = transNext;
		return nextState;
	}

	private int AwayState(double t) {

		// Get the current calendar
		Calendar now = this.sys.GetClaendar(t);

		if (this.runEntry) {
			double tHrs = Conversion.CalendarToHourOfDay(now);

			double randNum = 0;
			int resLeftInHouse = 0;

			// prepare the time based state transition variables.
			transTime = Double.POSITIVE_INFINITY;
			transNext = -1;
			// get the current level of motivation.
			curMotivation = prefs.motivationProb[OccupantParams.AWAY];

			// get the setpoint
			setpointTemp = sys.getSetpointTemp();

			// flip a coin
			randNum = rand.getBoundedRand(0, 1);
			// find out how many people are left in the house
			resLeftInHouse = sys.getTotalHome();
			// check the motivation probability and the people count.
			if (randNum < prefs.motivationProb[OccupantParams.AWAY] && resLeftInHouse <= 1)
				changeTemp = Math.ceil(prefs.comfortTemp[OccupantParams.AWAY] - setpointTemp);
			// specify to the room task that the resident is away
			sys.specifyAway();
		}

		// Get the inside temperature and setpoint
		insideTemp = sys.getTempInside();
		setpointTemp = sys.getSetpointTemp();

		// Make any setpoint adjustments that haven't already been made
		// Adjust the setpoint one press at a time.
		/*
		 * if( changeTemp > 0 ) { UserInterfaceArrowUp( task ); changeTemp -= 1;
		 * } if( changeTemp < 0 ) { UserInterfaceArrowDown( task ); changeTemp
		 * += 1; }
		 */
		if (changeTemp != 0) {
			sys.adjustSetpoint(changeTemp);
			changeTemp = 0;
		}

		int nextState = -1; // Default is no transition
		double transHour = 0;

		// for checking the transition times.
		double tHrs = Conversion.CalendarToHourOfDay(now);
		double leaveMod = 0;
		double arriveMod = 0;
		if (prefs.leaveTime[0] < prefs.arriveTime[0]) {
			if (tHrs > prefs.arriveTime[0])
				leaveMod = 24;
			else
				arriveMod = -24;
		}

		// time based transition
		if (transTime == Double.POSITIVE_INFINITY
				&& tHrs > (prefs.arriveTime[0] + arriveMod)
				&& tHrs < (prefs.leaveTime[0] + leaveMod)) {
			transHour = rand.getBoundedRand(prefs.arriveTime[0],
					prefs.arriveTime[0] + prefs.arriveTime[1])
					+ arriveMod;
			transNext = AWAKE_COMFORTABLE_STATE;
			transTime = t + (transHour - tHrs) * 3600;
		}

		// set the time based transition when past time.
		if (t > transTime)
			nextState = transNext;
		return nextState;
	}
	
	public boolean getWorking() {
		return prefs.getWorking();
	}

	public boolean getDayShift() {
		return prefs.getDayShift();
	}

	public double getWakeTime() {
		return prefs.getWakeTime();
	}

	public double getSleepTime() {
		return prefs.getSleepTime();
	}

	public double getLeaveTime() {
		return prefs.getLeaveTime();
	}

	public double getArriveTime() {
		return prefs.getArriveTime();
	}

	public double getComfortTemp() {
		return prefs.getComfortTemp();
	}

	public double getSleepTemp() {
		return prefs.getSleepTemp();
	}

	public double getAwayTemp() {
		return prefs.getAwayTemp();
	}
}
