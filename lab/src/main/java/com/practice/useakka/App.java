package com.practice.useakka;

import akka.actor.typed.ActorSystem;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.practice.useakka.actors.InMemoryAdditionalDatabase;
import com.practice.useakka.actors.InMemoryDishDatabase;
import com.practice.useakka.models.Additional;
import com.practice.useakka.models.Dish;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class App {

    private final ActorSystem<InMemoryDishDatabase.Command> inMemoryDishDatabaseSystem;
    private final ActorSystem<InMemoryAdditionalDatabase.Command> inMemoryAdditionalDatabaseSystem;

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new RootModule());
        App app = injector.getInstance(App.class);

        try {
            app.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws Exception {
        System.out.println(new String(App.class.getResourceAsStream("/logo").readAllBytes()));
        setupTestData();
    }

    private void setupTestData() {
        final ImmutableList<Dish> dishes = Lists.immutable.of(
                new Dish("Кофе", 40),
                new Dish("Черный чай", 35),
                new Dish("Зеленый чай", 38)
        );
        inMemoryDishDatabaseSystem.tell(new InMemoryDishDatabase.PutDishes(dishes));

        final ImmutableList<Additional> additionals = Lists.immutable.of(
                new Additional("Сахар", 0),
                new Additional("Молоко", 5)
        );
        inMemoryAdditionalDatabaseSystem.tell(new InMemoryAdditionalDatabase.PutAdditionals(additionals));
    }
}
