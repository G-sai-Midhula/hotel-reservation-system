package com.hotel.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }

    @Bean
    CommandLineRunner loadData(RoomRepository roomRepository) {
        return args -> {
            Room r1 = new Room();
            r1.setRoomType("Single");
            r1.setPrice(1500);
            r1.setAvailable(true);
            roomRepository.save(r1);

            Room r2 = new Room();
            r2.setRoomType("Double");
            r2.setPrice(2500);
            r2.setAvailable(true);
            roomRepository.save(r2);

            Room r3 = new Room();
            r3.setRoomType("Deluxe");
            r3.setPrice(4000);
            r3.setAvailable(false);
            roomRepository.save(r3);
        };
    }
}