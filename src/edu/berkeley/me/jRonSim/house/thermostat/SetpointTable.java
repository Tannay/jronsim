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
package edu.berkeley.me.jRonSim.house.thermostat;

import TranRunJLite.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Setpoint table entity.
 * 
 * @author William Burke <billstron@gmail.com>
 */
@SuppressWarnings("unchecked")
public class SetpointTable {

	private ArrayList<Setpoint>[] table = (ArrayList<Setpoint>[]) new ArrayList[7];

	/**
	 * Constructs the setpoint table.
	 * 
	 */
	public SetpointTable() {
		double Tsp = 75.0;
		int hour = 6;
		int min = 0;
		Setpoint.Label label = Setpoint.Label.MORNING;
		// table = new ArrayList[7];
		for (int day = 0; day < table.length; day++) {
			table[day] = new ArrayList<Setpoint>();
			Setpoint sp = new Setpoint(Tsp, hour, min, label);
			table[day].add(sp);
			// Tsp += 1;
		}
	}

	/**
	 * Replace the currently stored day with the one specified.
	 * 
	 * @param day
	 * @param tableDay
	 */
	public void ReplaceSetpointDay(int day, ArrayList<Setpoint> tableDay) {
		// clear the table
		table[day].clear();

		// make sure the entries in the tableDay are sorted properly.
		boolean sorted = false;
		while (!sorted) {
			// assume that it's sorted
			sorted = true;
			// move through the entire table looking for unsorted entries
			for (int i = 1; i < tableDay.size(); i++) {
				// locally store the times
				int[] time = tableDay.get(i - 1).getTime();
				double t0 = (double) time[Setpoint.HOUR]
						+ (double) time[Setpoint.MINUTE] / 60.0;
				time = tableDay.get(i).getTime();
				double t1 = (double) time[Setpoint.HOUR]
						+ (double) time[Setpoint.MINUTE] / 60.0;
				// if they are out of order
				if (t0 > t1) {
					// move them into better order
					Setpoint tempSp = tableDay.get(i - 1);
					tableDay.set(i - 1, tableDay.get(i));
					tableDay.set(i, tempSp);
					// indicate that it isn't yet sorted
					sorted = false;
				}
			}
		}
		// replace the day
		table[day] = tableDay;
	}

	/**
	 * Gets the setpoint for the given time.
	 * 
	 * @param cal
	 * @return
	 */
	public double getTsp(GregorianCalendar cal) {
		int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int dayPrev = day - 1;
		if (dayPrev < 0) {
			dayPrev = 6;
		}
		// System.out.println("day, preDay: " + day + ", " + dayPrev);
		double Tsp = table[dayPrev].get(table[dayPrev].size() - 1).getTsp();

		for (Setpoint sp : table[day]) {
			if (sp.isBefore(cal)) {
				// System.out.println("time: " + cal.get(Calendar.HOUR_OF_DAY));
				Tsp = sp.getTsp();
			}
		}
		return Tsp;
	}

	/**
	 * Test function.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		PrintWriter dataFile0 = null;
		try {
			FileWriter fW = new FileWriter("dataFile0.txt");
			dataFile0 = new PrintWriter(fW);
		} catch (IOException e) {
			System.out.println("IO Error " + e);
			System.exit(1); // File error -- quit
		}
		GregorianCalendar dStart = new GregorianCalendar(1977, 10, 2, 0, 0, 0);
		TrjTimeSim tm = new TrjTimeSim(dStart, 0);

		SetpointTable table = new SetpointTable();

		double t = 0;
		double dt = 60 * 30;
		double tNext = 0;
		double tStop = 48 * 3600;
		while (tm.getRunningTime() < tStop) {
			t = tm.getRunningTime();
			if (t >= tNext) {
				double Tsp = table.getTsp(tm.getCalendar(t));
				dataFile0.println(t + ", " + Tsp);
				tNext += dt;
			}
			tm.incrementRunningTime(dt);
		}
		dataFile0.close();
	}
}
