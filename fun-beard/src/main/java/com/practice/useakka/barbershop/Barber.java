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

class Barber extends AbstractBehavior<Barber.Command> {

    private final ActorRef<WaitingRoom.Response> waitingRoomResponseAdapter;

    public interface Command {
    }

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
        return Behaviors.setup(context -> new Barber(context, waitingRoom));
    }

    public Barber(ActorContext<Command> context, ActorRef<WaitingRoom.Command> waitingRoom) {
        super(context);
        this.waitingRoom = waitingRoom;
        barbershopEventAdapter = context.messageAdapter(Barbershop.Event.class, BarbershopEventWrapper::new);
        waitingRoomResponseAdapter = context.messageAdapter(WaitingRoom.Response.class, WaitingRoomResponseWrapper::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BarbershopEventWrapper.class, this::onBarbershopEvent)
                .onMessage(WaitingRoomResponseWrapper.class, this::onWaitingRoomResponse)
                .build();
    }

    private Behavior<Command> onWaitingRoomResponse(WaitingRoomResponseWrapper m) {
        if (m.response instanceof WaitingRoom.ClientResponse) {
            final WaitingRoom.ClientResponse response = (WaitingRoom.ClientResponse) m.response;
            response.client.ifPresent(this::serveClient);
        }
        return this;
    }

    private Behavior<Command> onBarbershopEvent(BarbershopEventWrapper m) {
        if (m.event instanceof Barbershop.ReceiveClientEvent) {
            waitingRoom.tell(new WaitingRoom.GetClient(waitingRoomResponseAdapter));
        }
        return this;
    }

    private void serveClient(Client client) {
        final Logger logger = getContext().getLog();
        logger.info("Барбер стрижет клиента {}", client);
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(300, 500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Барбер подстриг клиента {}", client);
    }

}
