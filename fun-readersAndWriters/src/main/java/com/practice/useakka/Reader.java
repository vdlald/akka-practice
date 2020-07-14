package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;

public class Reader {

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
        return Behaviors.setup(context -> new Reader(context, name).reader());
    }

    private final ActorContext<Reader.Command> context;
    private final String name;
    private final ActorRef<Mail.Response> messageAdapter;

    public Reader(ActorContext<Command> context, String name) {
        this.context = context;
        messageAdapter = context.messageAdapter(Mail.Response.class, WrappedMailCommand::new);
        this.name = name;
    }

    public Behavior<Command> reader() {
        return Behaviors.receive(Command.class)
                .onMessage(WrappedMailCommand.class, this::onWrappedMessageRespond)
                .onMessage(ReadMessage.class, this::onReadMessage)
                .build();
    }

    private Behavior<Command> onReadMessage(ReadMessage m) {
        m.readFrom.tell(new Mail.GetMessage(messageAdapter));
        return Behaviors.same();
    }

    private Behavior<Command> onWrappedMessageRespond(WrappedMailCommand m) {
        final Mail.MessageRespond messageRespond = m.command instanceof Mail.MessageRespond ? ((Mail.MessageRespond) m.command) : null;
        if (messageRespond != null) {
            context.getLog().info("{}: {}", name, messageRespond.message);
        }
        return Behaviors.same();
    }
}
