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
package sim;

import TranRunJLite.TrjTimeAccel;
import house.House;
import house.PctHouse;
import house.PctHouseRunnableAMI;
import gatewayComm.AmiCommSetup;

/**
 *
 * @author William Burke <billstron@gmail.com>
 */
public class PctHouseSimAMI
{

    /** Test function
     *
     * @param args
     */
    public static void main(String[] args)
    {
    	//setup network communications with the Gateway
    	AmiCommSetup ami = new AmiCommSetup();
    	
        double dt = 5.0;  // Used for samples that need a time delta
        double tFinal = 24 * 60 * 60;  // sec
        TrjTimeAccel tm = new TrjTimeAccel(100);
        House hs = new PctHouse(tm, true);

        PctHouseRunnableAMI runner = new PctHouseRunnableAMI(dt, tFinal, tm, hs, ami);
        Thread t = new Thread(runner);
        t.start();
        
        //get whole house energy usage, convert to XML string
        //send string to Gateway
        
    }
}