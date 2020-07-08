package com.practice.useakka;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

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

                    final ActorRef<BarberShop.Command> barberShop = context
                            .spawn(BarberShop.create(waitingRoomSize), "BarberShop");

                    final IntSupplier clientNumberGenerator = createIntGenerator();
                    executorService.scheduleAtFixedRate(() -> {
                        final Client client = new Client("Client " + clientNumberGenerator.getAsInt());
                        barberShop.tell(new BarberShop.ReceiveClient(client));
                    }, 0, 300 + random.nextInt(50, 100), TimeUnit.MILLISECONDS);

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
