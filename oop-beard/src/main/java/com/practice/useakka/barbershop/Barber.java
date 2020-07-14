package com.practice.useakka.barbershop;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.practice.useakka.pojo.Client;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

class Barber {

    private final ActorRef<WaitingRoom.Response> waitingRoomResponseAdapter;

    public interface Command {
    }
    private final ActorContext<Barber.Command> context;

    @RequiredArgsConstructor
    public static class BarbershopEventWrapper implements Command {
        public final Barbershop.Event event;
    }

    @RequiredArgsConstructor
    public static class WaitingRoomResponseWrapper implements Command {
        public final WaitingRoom.Response response;
    }

    private final ActorRef<Barbershop.Event> barbershopEventAdapter;
    private final ActorRef<WaitingRoom.Command> waitingRoom;

    public static Behavior<Command> create(ActorRef<WaitingRoom.Command> waitingRoom) {
        return Behaviors.setup(context -> new Barber(context, waitingRoom).barber());
    }

    public Barber(ActorContext<Command> context, ActorRef<WaitingRoom.Command> waitingRoom) {
        this.context = context;
        this.waitingRoom = waitingRoom;
        barbershopEventAdapter = context.messageAdapter(Barbershop.Event.class, BarbershopEventWrapper::new);
        waitingRoomResponseAdapter = context.messageAdapter(WaitingRoom.Response.class, WaitingRoomResponseWrapper::new);
    }

    private Behavior<Command> barber() {
        return Behaviors.receive(Command.class)
                .onMessage(BarbershopEventWrapper.class, this::onBarbershopEvent)
                .onMessage(WaitingRoomResponseWrapper.class, this::onWaitingRoomResponse)
                .build();
    }

    private Behavior<Command> onWaitingRoomResponse(WaitingRoomResponseWrapper m) {
        if (m.response instanceof WaitingRoom.ClientResponse) {
            final WaitingRoom.ClientResponse response = (WaitingRoom.ClientResponse) m.response;
            response.client.ifPresent(this::serveClient);
        }
        return Behaviors.same();
    }

    private Behavior<Command> onBarbershopEvent(BarbershopEventWrapper m) {
        if (m.event instanceof Barbershop.ReceiveClientEvent) {
            waitingRoom.tell(new WaitingRoom.GetClient(waitingRoomResponseAdapter));
        }
        return Behaviors.same();
    }

    private void serveClient(Client client) {
        final Logger logger = context.getLog();
        logger.info("Барбер стрижет клиента {}", client);
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(300, 500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Барбер подстриг клиента {}", client);
        waitingRoom.tell(new WaitingRoom.GetClient(waitingRoomResponseAdapter));
    }

}
