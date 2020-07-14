package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

public class Writer extends AbstractBehavior<Writer.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class WriteMessage implements Command {
        public final ActorRef<Mail.Command> writeTo;
        public final String message;
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Writer::new);
    }

    public Writer(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(WriteMessage.class, this::onWriteMessage)
                .build();
    }

    private Behavior<Command> onWriteMessage(WriteMessage m) {
        m.writeTo.tell(new Mail.PushMessage(m.message));
        return this;
    }
}
