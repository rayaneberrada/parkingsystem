package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import static org.mockito.ArgumentMatchers.*;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
@DisplayName("tests checking the FareCalculatorServic class")
public class FareCalculatorServiceTest {
	
	@Mock
	TicketDAO ticketDAO;

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Nested
    @DisplayName("Tests checking fares for CAR type")
    class  calculateFareCar {
        @Test
        @DisplayName("test fares for a car parked one hour")
        public void calculateFareCarForAnHour(){
            // GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN
            fareCalculatorService.calculateFare(ticket);

            // THEN
            assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
        }

        @Test
        @DisplayName("test fares for a car parked thirty minutes")
        public void calculateFareCarWithLessThanThirtyOnEMinutesParkingTime() {
            // GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  30 * 60 * 1000) );//30 minutes parking time should give 0 parking fare
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN
            fareCalculatorService.calculateFare(ticket);

            // THEN
            assertEquals( (0.75 * Fare.CAR_RATE_FIRST_THIRTY_MINUTES) , ticket.getPrice());
        }

        @Test
        @DisplayName("test fares isn't free over thirty minutes")
        public void calculateFareCarWithMoreThanThirtyMinutesParkingTime() {
            // GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  31 * 60 * 1000) );//31 minutes parking time should start charging
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN
            fareCalculatorService.calculateFare(ticket);

            // THEN
            assertEquals( ((float)31/60 * Fare.CAR_RATE_PER_HOUR) , (double)ticket.getPrice());
        }

        @Test
        @DisplayName("test fares for a car parked between one hour and thirty minutes use normal fee")
        public void calculateFareCarWithLessThanOneHourParkingTime(){
            // GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );
            //45 minutes parking time should give 3/4th parking fare using RATE_PER_HOUR constant
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN
            fareCalculatorService.calculateFare(ticket);

            // THEN
            assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
        }

        @Test
        @DisplayName("test fares for a car parked between one hour and thirty minutes doesn't use free fee")
        public void failCalculateFareCarWithLessThanOneHourParkingTime(){
            // GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );
            //45 minutes parking time should give 3/4th parking fare using RATE_PER_HOUR constant
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN
            fareCalculatorService.calculateFare(ticket);

            // THEN
            assertNotEquals( (0.75 * Fare.CAR_RATE_FIRST_THIRTY_MINUTES) , ticket.getPrice());
        }

        @Test
        @DisplayName("test fares for a car parked more than a day")
        public void calculateFareCarWithMoreThanADayParkingTime(){
            //GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN
            fareCalculatorService.calculateFare(ticket);

            // THEN
            assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
        }

        @Test
        @DisplayName("test fares for a car parked forty five minutes with a discount")
        public void calculateFareCarWithLessThanOneHourParkingTimeAndDiscount(){
            // GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//should give 95% of the price of a normal 45 minutes parking fare
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket.setPrice(0);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN
            fareCalculatorService.calculateFare(ticket);

            // THEN
            assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR)*0.95 , ticket.getPrice());
        }

    }

    @Nested
    @DisplayName("Tests checking fares for BIKE type")
    class calculateFareBike {
        @Test
        @DisplayName("test fares for a bike parked one hour")
        public void calculateFareBikeForAnHour(){
            // GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN
            fareCalculatorService.calculateFare(ticket);

            // THEN
            assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
        }

        @Test
        @DisplayName("test checking exception raised when parking time later than now")
        public void failCalculateFareBikeWithFutureInTime(){
            // GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN AND THEN
            assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
        }

        @Test
        @DisplayName("test fares for a bike parked less than an hour")
        public void calculateFareBikeWithLessThanOneHourParkingTime(){
            // GIVEN
            Date inTime = new Date();
            inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
            Date outTime = new Date();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);

            // WHEN
            fareCalculatorService.calculateFare(ticket);

            // THEN
            assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
        }
    }


    @Test
    @DisplayName("test checking an exception is raised when vehicle type isn't defined")
    public void calculateFareUnkownType(){
        // GIVEN
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // WHEN AND THEN
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }
}
