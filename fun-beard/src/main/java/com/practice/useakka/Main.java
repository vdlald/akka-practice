package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import com.practice.useakka.barbershop.Barbershop;
import com.practice.useakka.pojo.Client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

public class Main {

    private static final int waitingRoomSize = 5;

    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    final ScheduledExecutorService executorService = Executors
                            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
                    final ThreadLocalRandom random = ThreadLocalRandom.current();

                    final ActorRef<Barbershop.Command> barbershop = context.spawn(Barbershop.create(), "Barbershop");

                    final IntSupplier intGenerator = createIntGenerator();
                    executorService.scheduleAtFixedRate(() -> {
                        barbershop.tell(new Barbershop.ReceiveClient(
                                new Client("Client" + intGenerator.getAsInt())));
                    }, 0, random.nextInt(300, 400), TimeUnit.MILLISECONDS);

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    public static void main(String[] args) {
        ActorSystem.create(Main.create(), "Main");
    }

    private static IntSupplier createIntGenerator() {
        return new IntSupplier() {
            int i = 0;

            @Override
            public int getAsInt() {
                i++;
                return i;
            }
        };
    }
}
