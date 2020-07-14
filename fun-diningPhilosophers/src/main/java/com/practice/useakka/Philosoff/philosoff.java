package com.practice.useakka.Philosoff;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import com.practice.useakka.pojo.Client;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

public class philosoff {

    private final ActorRef<waiting.Response> waitingRoomResponseAdapter;

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class vilkiEventWrapper implements Command {
        public final vilki.Event event;
    }

    @RequiredArgsConstructor
    public static class waitingResponseWrapper implements Command {
        public final waiting.Response response;
    }

    private final ActorRef<vilki.Event> vilkiEventAdapter;
    private final ActorRef<waiting.Command> waiting;

    public static Behavior<Command> create(ActorRef<waiting.Command> waiting) {
        return Behaviors.setup(context -> new philosoff(context, waiting).philosoff());
    }

    private final ActorContext<philosoff.Command> context;

    public philosoff(ActorContext<Command> context, ActorRef<waiting.Command> waiting) {
        this.context = context;
        this.waiting = waiting;
        vilkiEventAdapter = context.messageAdapter(vilki.Event.class, vilkiEventWrapper::new);
        waitingRoomResponseAdapter = context.messageAdapter(waiting.Response.class, waitingResponseWrapper::new);
    }

    private Behavior<Command> philosoff() {
        return Behaviors.receive(Command.class)
                .onMessage(vilkiEventWrapper.class, this::onvilkiEvent)
                .onMessage(waitingResponseWrapper.class, this::onwaitingResponse)
                .build();
    }

    private Behavior<Command> onwaitingResponse(waitingResponseWrapper m) {
        if (m.response instanceof waiting.ClientResponse) {
            final waiting.ClientResponse response = (waiting.ClientResponse) m.response;
            response.client.ifPresent(this::serveClient);
        }
        return Behaviors.same();
    }

    private Behavior<Command> onvilkiEvent(vilkiEventWrapper m) {
        if (m.event instanceof vilki.ReceiveClientEvent) {
            waiting.tell(new waiting.GetClient(waitingRoomResponseAdapter));
        }
        return Behaviors.same();
    }

    private void serveClient(Client client) {
        final Logger logger = context.getLog();
        logger.info("Филосов ищет вилку {}", client);
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(300, 500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("филосов за столом {}", client);
        waiting.tell(new waiting.GetClient(waitingRoomResponseAdapter));
    }

}