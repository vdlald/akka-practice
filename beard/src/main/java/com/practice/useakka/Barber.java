package com.practice.useakka;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

public class Barber extends AbstractBehavior<Barber.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class ServeClient implements Command {
        public final Client client;
    }

    public interface Event {
    }

    public enum ClientServed implements Event {
        INSTANCE
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Barber::new);
    }

    public Barber(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ServeClient.class, this::onServeClient)
                .build();
    }

    private Behavior<Command> onServeClient(ServeClient m) {
        final Logger logger = getContext().getLog();
        logger.info("Сейчас барбер обслуживает клиента " + m.client.getName());
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(300, 500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Барбер обслужил клиента " + m.client.getName());
        // stop here: циклическая зависимость
//        getContext().pa
        return this;
    }
}
