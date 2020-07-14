package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

public class Writer {

    public interface Command {
    }

    private final ActorContext<Writer.Command> context;

    @RequiredArgsConstructor
    public static class WriteMessage implements Command {
        public final ActorRef<Mail.Command> writeTo;
        public final String message;
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> new Writer(context).writer());
    }

    public Writer(ActorContext<Command> context) {
        this.context = context;
    }

    public Behavior<Command> writer() {
        return Behaviors.receive(Command.class)
                .onMessage(WriteMessage.class, this::onWriteMessage)
                .build();
    }

    private Behavior<Command> onWriteMessage(WriteMessage m) {
        m.writeTo.tell(new Mail.PushMessage(m.message));
        return Behaviors.same();
    }
}
