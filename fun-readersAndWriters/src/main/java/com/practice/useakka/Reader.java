package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

public class Reader extends AbstractBehavior<Reader.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class ReadMessage implements Command {
        public final ActorRef<Mail.Command> readFrom;
    }

    @RequiredArgsConstructor
    public static class WrappedMailCommand implements Command {
        public final Mail.Response command;
    }

    public static Behavior<Command> create(String name) {
        return Behaviors.setup(context -> new Reader(context, name));
    }

    private final String name;
    private final ActorRef<Mail.Response> messageAdapter;

    public Reader(ActorContext<Command> context, String name) {
        super(context);
        messageAdapter = context.messageAdapter(Mail.Response.class, WrappedMailCommand::new);
        this.name = name;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(WrappedMailCommand.class, this::onWrappedMessageRespond)
                .onMessage(ReadMessage.class, this::onReadMessage)
                .build();
    }

    private Behavior<Command> onReadMessage(ReadMessage m) {
        m.readFrom.tell(new Mail.GetMessage(messageAdapter));
        return this;
    }

    private Behavior<Command> onWrappedMessageRespond(WrappedMailCommand m) {
        final Mail.MessageRespond messageRespond = m.command instanceof Mail.MessageRespond ? ((Mail.MessageRespond) m.command) : null;
        if (messageRespond != null) {
            getContext().getLog().info("{}: {}", name, messageRespond.message);
        }
        return this;
    }
}
