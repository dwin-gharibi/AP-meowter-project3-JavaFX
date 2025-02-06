package ir.ac.kntu.Meowter.util;

import ir.ac.kntu.Meowter.model.Ticket;
import ir.ac.kntu.Meowter.model.TicketStatus;
import ir.ac.kntu.Meowter.repository.TicketRepository;

import java.util.concurrent.*;

public class TicketAutoResponder {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private static final ConcurrentHashMap<Long, ScheduledFuture<?>> pendingTickets = new ConcurrentHashMap<>();
    private static final TicketRepository ticketRepository = new TicketRepository();

    public static void monitorTicket(Ticket ticket) {
        if (ticket.getStatus() != TicketStatus.SUBMITTED) {
            return;
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            Ticket latestTicket = ticketRepository.findById(ticket.getId());

            if (latestTicket != null && latestTicket.getResponse().equals("There is no response yet.")) {
                latestTicket.setResponse("We will contact you soon.");
                latestTicket.setStatus(TicketStatus.PENDING);
                ticketRepository.update(latestTicket);
                System.out.println("Auto-response sent for Ticket ID: " + latestTicket.getId());
            }

            pendingTickets.remove(ticket.getId());
        }, 30, TimeUnit.SECONDS);

        pendingTickets.put(ticket.getId(), future);
    }

    public static void cancelAutoResponse(Long ticketId) {
        ScheduledFuture<?> future = pendingTickets.remove(ticketId);
        if (future != null) {
            future.cancel(false);
        }
    }
}
