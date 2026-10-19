package itm.oss.booking;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserSummaryServiceTest {

    private static final double DELTA = 1e-9;

    private static Booking booking(int id, String userId, String roomId,
            String start, String end, BookingStatus status) {
        return new Booking(id, userId, roomId,
                LocalDateTime.parse(start), LocalDateTime.parse(end),
                2, "Study", status);
    }

    private static void assertSummary(UserBookingSummary summary,
            int count, long total, int rooms, double average) {
        assertAll(
                () -> assertEquals(count, summary.getBookingCount(), "bookingCount"),
                () -> assertEquals(total, summary.getTotalMinutes(), "totalMinutes"),
                () -> assertEquals(rooms, summary.getDistinctRooms(), "distinctRooms"),
                () -> assertEquals(average, summary.getAverageMinutes(), DELTA, "averageMinutes"));
    }

    // Required by Issue 6: one user, more than one room, at least one cancelled booking.
    @Test
    void summarizesActiveBookingsInMultipleRoomsAndIgnoresCancelled() {
        List<Booking> bookings = List.of(
                booking(1, "s2026001", "R101", "2026-10-02T10:00", "2026-10-02T11:00", BookingStatus.ACTIVE),    // 60
                booking(2, "s2026001", "R102", "2026-10-02T13:00", "2026-10-02T14:30", BookingStatus.ACTIVE),    // 90
                booking(3, "s2026001", "R101", "2026-10-03T09:00", "2026-10-03T09:30", BookingStatus.ACTIVE),    // 30, same room again
                booking(4, "s2026001", "R201", "2026-10-03T14:00", "2026-10-03T16:00", BookingStatus.CANCELLED), // ignored
                booking(5, "s2026002", "R202", "2026-10-02T10:00", "2026-10-02T12:00", BookingStatus.ACTIVE));   // other user

        UserBookingSummary summary = UserSummaryService.summarize(bookings, "s2026001");

        // 3 bookings, 60+90+30 = 180 minutes, rooms {R101, R102}, 180/3 = 60.0
        assertSummary(summary, 3, 180, 2, 60.0);
    }

    @Test
    void matchesUserIdCaseInsensitivelyAfterTrimming() {
        List<Booking> bookings = List.of(
                booking(1, "S2026001", "R101", "2026-10-02T10:00", "2026-10-02T11:00", BookingStatus.ACTIVE),
                booking(2, "s2026001", "R102", "2026-10-02T13:00", "2026-10-02T14:00", BookingStatus.ACTIVE));

        UserBookingSummary summary = UserSummaryService.summarize(bookings, "  s2026001  ");

        assertSummary(summary, 2, 120, 2, 60.0);
    }

    @Test
    void averageUsesFloatingPointDivision() {
        List<Booking> bookings = List.of(
                booking(1, "s2026001", "R101", "2026-10-02T10:00", "2026-10-02T11:00", BookingStatus.ACTIVE),  // 60
                booking(2, "s2026001", "R101", "2026-10-02T13:00", "2026-10-02T13:45", BookingStatus.ACTIVE)); // 45

        UserBookingSummary summary = UserSummaryService.summarize(bookings, "s2026001");

        // integer division would give 52
        assertSummary(summary, 2, 105, 1, 52.5);
    }

    @Test
    void returnsZeroSummaryWhenUserHasOnlyCancelledBookings() {
        List<Booking> bookings = List.of(
                booking(1, "s2026001", "R101", "2026-10-02T10:00", "2026-10-02T11:00", BookingStatus.CANCELLED),
                booking(2, "s2026002", "R102", "2026-10-02T10:00", "2026-10-02T11:00", BookingStatus.ACTIVE));

        UserBookingSummary summary = UserSummaryService.summarize(bookings, "s2026001");

        assertSummary(summary, 0, 0, 0, 0.0);
    }

    @Test
    void returnsZeroSummaryForUnknownUserOrEmptyList() {
        List<Booking> bookings = List.of(
                booking(1, "s2026001", "R101", "2026-10-02T10:00", "2026-10-02T11:00", BookingStatus.ACTIVE));

        assertSummary(UserSummaryService.summarize(bookings, "s9999999"), 0, 0, 0, 0.0);
        assertSummary(UserSummaryService.summarize(List.of(), "s2026001"), 0, 0, 0, 0.0);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rejectsNullOrBlankUserId(String userId) {
        List<Booking> bookings = List.of(
                booking(1, "s2026001", "R101", "2026-10-02T10:00", "2026-10-02T11:00", BookingStatus.ACTIVE));

        assertThrows(IllegalArgumentException.class,
                () -> UserSummaryService.summarize(bookings, userId));
    }
}
