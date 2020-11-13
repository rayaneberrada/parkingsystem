package com.parkit.parkingsystem.service;

import java.util.concurrent.TimeUnit;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inMillies = ticket.getInTime().getTime();
        long outMillies = ticket.getOutTime().getTime();

        /** duration convert the difference between leaving and arriving time in minutes and divide by 60 to get 
         * 	the proportion of hours it represent
         **/
        float duration = (float)TimeUnit.MINUTES.convert((outMillies - inMillies), TimeUnit.MILLISECONDS) / 60;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
            	float fare = (float) (duration <= 0.5 ? Fare.CAR_RATE_FIRST_THIRTY_MINUTES : Fare.CAR_RATE_PER_HOUR);
            	
            	if(ticket.getPrice() == -1) {
            		ticket.setPrice(duration * fare);
            	} else {
            		ticket.setPrice(duration * fare * 0.95);
            	}
                break;
            }
            case BIKE: {
            	double fare = duration <= 0.5 ? Fare.BIKE_RATE_FIRST_THIRTY_MINUTES : Fare.BIKE_RATE_PER_HOUR;
            	if(ticket.getPrice() == -1) {
            		ticket.setPrice(duration * fare);
            	} else {
            		ticket.setPrice(duration * fare * 0.95);
            	}
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}