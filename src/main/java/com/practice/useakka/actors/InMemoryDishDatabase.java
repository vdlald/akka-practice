package com.practice.useakka.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.practice.useakka.models.Dish;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MutableMap;

import java.util.Optional;

public class InMemoryDishDatabase extends AbstractBehavior<InMemoryDishDatabase.Command> {

    public interface Command {
    }

    @RequiredArgsConstructor
    public static class PutDish implements Command {
        public final Dish dish;
    }

    @RequiredArgsConstructor
    public static class PutDishes implements Command {
        public final ImmutableList<Dish> dishes;
    }

    @RequiredArgsConstructor
    public static class GetDish implements Command {
        public final String name;
        public final ActorRef<RespondDish> replyTo;
    }

    @RequiredArgsConstructor
    public static class RespondDish {
        public final Optional<Dish> additional;
    }

    private final MutableMap<String, Dish> map;

    public static Behavior<Command> create() {
        return Behaviors.setup(InMemoryDishDatabase::new);
    }

    public InMemoryDishDatabase(ActorContext<Command> context) {
        super(context);
        map = Maps.mutable.empty();
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PutDish.class, this::onPutDish)
                .onMessage(PutDishes.class, this::onPutDishes)
                .onMessage(GetDish.class, this::onGetDish)
                .build();
    }

    private Behavior<Command> onGetDish(GetDish command) {
        final Optional<Dish> dish = Optional.ofNullable(map.get(command.name));
        command.replyTo.tell(new RespondDish(dish));
        return this;
    }

    private Behavior<Command> onPutDish(PutDish command) {
        final Dish dish = command.dish;
        map.put(dish.getName(), dish);
        return this;
    }

    private Behavior<Command> onPutDishes(PutDishes command) {
        command.dishes.forEach(dish -> map.put(dish.getName(), dish));
        return this;
    }
}
