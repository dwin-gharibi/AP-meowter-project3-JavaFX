package ir.ac.kntu.Meowter.service;

import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.model.Ticket;
import ir.ac.kntu.Meowter.model.TicketStatus;
import ir.ac.kntu.Meowter.model.TicketSubject;
import ir.ac.kntu.Meowter.repository.TicketRepository;
import ir.ac.kntu.Meowter.util.TicketAutoResponder;

import java.util.List;

public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService() {
        this.ticketRepository = new TicketRepository();
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket createTicket(String description, TicketSubject subject, String username) {
        Ticket ticket = new Ticket(description, subject, username);
        ticketRepository.save(ticket);
        TicketAutoResponder.monitorTicket(ticket);
        return ticket;
    }

    public List<Ticket> getUserTickets(String username) {
        return ticketRepository.findByUsername(username);
    }

    public void respondToTicket(Ticket ticket, String response) {
        ticket.setResponse(response);
        ticket.setStatus(TicketStatus.PENDING);
        ticketRepository.update(ticket);
        TicketAutoResponder.cancelAutoResponse(ticket.getId());
    }

    public void closeTicket(Ticket ticket) {
        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.update(ticket);
    }
}
