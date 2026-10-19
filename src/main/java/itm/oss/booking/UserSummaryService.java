package itm.oss.booking;

import java.time.Duration;
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
        int bookingCount = 0;
        long totalMinutes = 0;
        List<String> distinctRooms = new ArrayList<>();
        double averageMinutes = 0;

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
            totalMinutes += Duration.between(booking.getStart(), booking.getEnd()).toMinutes();
            
        }

        if (bookingCount == 0) {
            averageMinutes = 0;
        } else {
            averageMinutes = (double) totalMinutes / bookingCount;
        }

        return new UserBookingSummary(bookingCount, totalMinutes, distinctRooms.size(), averageMinutes);
    }
}
