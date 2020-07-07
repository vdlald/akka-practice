package com.practice.useakka;

import akka.actor.typed.ActorSystem;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class App {

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
    }
}
