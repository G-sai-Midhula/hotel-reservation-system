package com.hotel.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }
    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("userRole"));
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/rooms")
    public String rooms(Model model, HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";

        List<Room> roomList = roomRepository.findAll();
        Map<String, Long> roomCounts = roomList.stream()
                .filter(Room::isAvailable)
                .collect(Collectors.groupingBy(Room::getRoomType, Collectors.counting()));
        Map<String, Double> roomPrices = roomList.stream()
                .collect(Collectors.toMap(Room::getRoomType, Room::getPrice, (p1, p2) -> p1));

        model.addAttribute("roomCounts", roomCounts);
        model.addAttribute("roomPrices", roomPrices);
        return "rooms";
    }

    @GetMapping("/booking")
    public String bookingForm(Model model, HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";

        List<Room> roomList = roomRepository.findAll().stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
        model.addAttribute("rooms", roomList);
        return "booking";
    }

    @PostMapping("/booking")
    public String saveBooking(@RequestParam String customerName,
                               @RequestParam String checkInDate,
                               @RequestParam String checkOutDate,
                               @RequestParam Long roomId,
                               HttpSession session,
                               Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";

        Room room = roomRepository.findById(roomId).orElse(null);

        if (room == null || !room.isAvailable()) {
            model.addAttribute("error", "Sorry, this room is no longer available.");
            return "redirect:/booking";
        }

        Booking booking = new Booking();
        booking.setCustomerName(customerName);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setRoom(room);
        bookingRepository.save(booking);

        room.setAvailable(false);
        roomRepository.save(room);

        return "redirect:/my-bookings";
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model, HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";

        List<Booking> bookingList = bookingRepository.findAll();
        model.addAttribute("bookings", bookingList);
        return "my-bookings";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model, HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/rooms"; 

        List<Room> roomList = roomRepository.findAll();
        List<Booking> bookingList = bookingRepository.findAll();
        model.addAttribute("rooms", roomList);
        model.addAttribute("bookings", bookingList);
        return "admin";
    }

    @PostMapping("/admin/add-room")
    public String addRoom(@RequestParam String roomType,
                           @RequestParam double price,
                           @RequestParam boolean available,
                           HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/rooms";

        Room room = new Room();
        room.setRoomType(roomType);
        room.setPrice(price);
        room.setAvailable(available);
        roomRepository.save(room);
        return "redirect:/admin";
    }

    @GetMapping("/admin/delete-room/{id}")
    public String deleteRoom(@PathVariable Long id, HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!isAdmin(session)) return "redirect:/rooms";

        roomRepository.deleteById(id);
        return "redirect:/admin";
    }
}