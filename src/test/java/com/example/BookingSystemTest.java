package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingSystemTest {

    @Mock
    TimeProvider timeProvider;

    @Mock
    RoomRepository roomRepository;

    @Mock
    NotificationService notificationService;

    @Mock
    Room room;

    @InjectMocks
    BookingSystem bookingSystem;

    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
    private final LocalDateTime start = now.plusDays(1);
    private final LocalDateTime end = start.plusHours(2);

    // ------------------- bookRoom -------------------

    @Test
    @DisplayName("Should successfully book room when available")
    void shouldBookRoomSuccessfully() throws NotificationException {
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById("Room1")).thenReturn(Optional.of(room));
        when(room.isAvailable(start, end)).thenReturn(true);

        boolean result = bookingSystem.bookRoom("Room1", start, end);

        assertThat(result).isTrue();
        verify(room).addBooking(any());
        verify(roomRepository).save(room);
        verify(notificationService).sendBookingConfirmation(any());
    }

    @Test
    @DisplayName("Should return false when room is not available")
    void shouldReturnFalseWhenRoomUnavailable() throws NotificationException {
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById("Room1")).thenReturn(Optional.of(room));
        when(room.isAvailable(start, end)).thenReturn(false);

        boolean result = bookingSystem.bookRoom("Room1", start, end);

        assertThat(result).isFalse();
        verify(room, never()).addBooking(any());
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when booking in the past")
    void shouldThrowWhenBookingInPast() {
        when(timeProvider.getCurrentTime()).thenReturn(now);

        assertThatThrownBy(() ->
                bookingSystem.bookRoom("Room1", now.minusHours(1), end)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when end time is before start time")
    void shouldThrowWhenEndBeforeStart() {
        when(timeProvider.getCurrentTime()).thenReturn(now);

        assertThatThrownBy(() ->
                bookingSystem.bookRoom("Room1", start, start.minusHours(1))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when room does not exist")
    void shouldThrowWhenRoomNotFound() {
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById("RoomX")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookingSystem.bookRoom("RoomX", start, end)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should still succeed even if notification fails")
    void shouldIgnoreNotificationFailure() throws NotificationException {
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findById("Room1")).thenReturn(Optional.of(room));
        when(room.isAvailable(start, end)).thenReturn(true);
        doThrow(new NotificationException("Fail"))
                .when(notificationService).sendBookingConfirmation(any());

        boolean result = bookingSystem.bookRoom("Room1", start, end);

        assertThat(result).isTrue();
        verify(roomRepository).save(room);
    }

    // ------------------- getAvailableRooms -------------------

    @Test
    @DisplayName("Should return only available rooms")
    void shouldReturnAvailableRooms() {
        when(room.isAvailable(start, end)).thenReturn(true);
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> result = bookingSystem.getAvailableRooms(start, end);

        assertThat(result).containsExactly(room);
    }

    @Test
    @DisplayName("Should throw exception when end is before start (getAvailableRooms)")
    void shouldThrowWhenEndBeforeStartInGetAvailableRooms() {
        assertThatThrownBy(() ->
                bookingSystem.getAvailableRooms(end, start)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------- cancelBooking -------------------

    @Test
    @DisplayName("Should successfully cancel future booking")
    void shouldCancelBookingSuccessfully() throws NotificationException {
        Booking booking = mock(Booking.class);

        when(room.hasBooking("B1")).thenReturn(true);
        when(room.getBooking("B1")).thenReturn(booking);
        when(booking.getStartTime()).thenReturn(start);
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findAll()).thenReturn(List.of(room));

        boolean result = bookingSystem.cancelBooking("B1");

        assertThat(result).isTrue();
        verify(room).removeBooking("B1");
        verify(roomRepository).save(room);
        verify(notificationService).sendCancellationConfirmation(booking);
    }

    @Test
    @DisplayName("Should return false when booking does not exist")
    void shouldReturnFalseWhenBookingNotFound() {
        when(roomRepository.findAll()).thenReturn(List.of());

        boolean result = bookingSystem.cancelBooking("UNKNOWN");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when cancelling started booking")
    void shouldThrowWhenCancellingStartedBooking() {
        Booking booking = mock(Booking.class);

        when(room.hasBooking("B1")).thenReturn(true);
        when(room.getBooking("B1")).thenReturn(booking);
        when(booking.getStartTime()).thenReturn(now.minusMinutes(10));
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(roomRepository.findAll()).thenReturn(List.of(room));

        assertThatThrownBy(() ->
                bookingSystem.cancelBooking("B1")
        ).isInstanceOf(IllegalStateException.class);
    }
}
