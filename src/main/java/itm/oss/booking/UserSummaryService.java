package itm.oss.booking;

import java.util.ArrayList;
import java.util.List;

public final class UserSummaryService {

    private UserSummaryService() {
    }

    /**
     * Issue 6.
     */
    public static UserBookingSummary summarize(List<Booking> bookings, String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }

        String requestedUserId = userId.trim();
        int bookingCount;
        long totalMinutes;
        List<String> distinctRooms = new ArrayList<>();
        double averageMinutes;

        for (Booking booking : bookings) {
            if (booking.getStatus() != BookingStatus.ACTIVE) {
                continue;  // not count for not ACTIVE bookings
            }

            if (!booking.getUserId().equalsIgnoreCase(requestedUserId)) {
                continue;  // only userId's bookings
            }

            if(!distinctRooms.contains(booking.getRoomId())) {
                distinctRooms.add(booking.getRoomId());
            }

            bookingCount++;
            totalMinutes += Duration.between(start, end).toMinutes();
            
        }

        if (bookingCount == 0) {
            averageMinutes = 0;
        } else {
            averageMinutes = (float) totalMinutes / bookingCount;
        }

        return new UserBookingSummary(bookingCount, totalMinutes, rooms.size(), averageMinutes);
    }
}
