package com.practice.useakka.barbershop;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.practice.useakka.pojo.Client;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;
import java.util.UUID;

public class Barbershop {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class ReceiveClient implements Command {
        public final Client client;
    }

    public interface Event {
    }

    public enum ReceiveClientEvent implements Event {
        INSTANCE
    }

    private final ActorContext<Barbershop.Command> context;
    private final ActorRef<Barber.Command> barber;
    private final ActorRef<WaitingRoom.Command> waitingRoom;

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> new Barbershop(context).barbershop());
    }

    public Barbershop(ActorContext<Command> context) {
        this.context = context;
        this.waitingRoom = context.spawn(WaitingRoom.create(new ArrayDeque<>()), "WaitingRoom-" + UUID.randomUUID());
        this.barber = context.spawn(Barber.create(waitingRoom), "Barber-" + UUID.randomUUID());
    }

    private Behavior<Command> barbershop() {
        return Behaviors.receive(Command.class)
                .onMessage(ReceiveClient.class, this::onReceiveClient)
                .build();
    }

    private Behavior<Command> onReceiveClient(ReceiveClient m) {
        context.getLog().info("Пришел клиент {}", m.client);
        waitingRoom.tell(new WaitingRoom.AddClient(m.client));
        barber.tell(new Barber.BarbershopEventWrapper(ReceiveClientEvent.INSTANCE));
        return Behaviors.same();
    }

}