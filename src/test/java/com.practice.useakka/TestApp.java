package com.practice.useakka;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.practice.useakka.actors.InMemoryAdditionalDatabase;
import com.practice.useakka.actors.InMemoryDishDatabase;
import com.practice.useakka.models.Additional;
import com.practice.useakka.models.Dish;
import com.practice.useakka.pojo.DishDto;
import com.practice.useakka.pojo.Order;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestApp {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger("Test");

    private static ActorRef<InMemoryDishDatabase.Command> inMemoryDishDatabaseSystem;
    private static ActorRef<InMemoryAdditionalDatabase.Command> inMemoryAdditionalDatabaseSystem;
    private static Dish coffee;
    private static Additional milk;

    @BeforeAll
    static void setUpData() {
        inMemoryDishDatabaseSystem = testKit.spawn(InMemoryDishDatabase.create());
        inMemoryAdditionalDatabaseSystem = testKit.spawn(InMemoryAdditionalDatabase.create());

        coffee = new Dish("Кофе", 40);
        final ImmutableList<Dish> dishes = Lists.immutable.of(
                coffee,
                new Dish("Черный чай", 35),
                new Dish("Зеленый чай", 38)
        );
        inMemoryDishDatabaseSystem.tell(new InMemoryDishDatabase.PutDishes(dishes));

        milk = new Additional("Молоко", 5);
        final ImmutableList<Additional> additionals = Lists.immutable.of(
                new Additional("Сахар", 0),
                milk
        );
        inMemoryAdditionalDatabaseSystem.tell(new InMemoryAdditionalDatabase.PutAdditionals(additionals));
    }

    @Test
    void testCreateOrder() {
        final TestProbe<InMemoryDishDatabase.RespondDish> dishTestProbe = testKit
                .createTestProbe(InMemoryDishDatabase.RespondDish.class);
        final TestProbe<InMemoryAdditionalDatabase.RespondAdditional> additionalTestProbe = testKit
                .createTestProbe(InMemoryAdditionalDatabase.RespondAdditional.class);

        inMemoryDishDatabaseSystem.tell(new InMemoryDishDatabase.GetDish("Кофе", dishTestProbe.getRef()));
        inMemoryAdditionalDatabaseSystem.tell(
                new InMemoryAdditionalDatabase.GetAdditional("Молоко", additionalTestProbe.getRef()));

        final InMemoryDishDatabase.RespondDish respondDish = dishTestProbe.receiveMessage();
        final InMemoryAdditionalDatabase.RespondAdditional respondAdditional = additionalTestProbe.receiveMessage();

        final Order result = Order.newBuilder()
                .addDish(DishDto.of(respondDish.dish.get(), respondAdditional.additional.get()))
                .build();

        assertEquals(
                Order.newBuilder()
                        .addDish(DishDto.of(coffee, milk))
                        .build()
                        .toString(),
                result.toString()
        );
    }

}
