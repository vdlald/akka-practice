package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

public class BarberShop extends AbstractBehavior<BarberShop.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class ReceiveClient implements Command {
        public final Client client;
    }

    private final ActorRef<Manager.Command> manager;
    private final ActorRef<Barber.Command> barber;
    private final ActorRef<WaitingRoom.Command> waitingRoom;

    public static Behavior<BarberShop.Command> create(int waitingRoomSize) {
        return Behaviors.setup(context -> new BarberShop(context, waitingRoomSize));
    }

    public BarberShop(ActorContext<Command> context, int waitingRoomSize) {
        super(context);
        this.manager = context.spawn(Manager.create(), "Manager");
        this.barber = context.spawn(Barber.create(), "Barber");
        this.waitingRoom = context.spawn(WaitingRoom.create(waitingRoomSize), "WaitingRoom");
    }

    @Override
    public Receive<BarberShop.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReceiveClient.class, this::onReceiveClient)
                .build();
    }

    private Behavior<Command> onReceiveClient(ReceiveClient m) {
        manager.tell(new Manager.ServeClient(m.client));
        return this;
    }

}
