# Module info:
 Чтобы использовать Akka Actors, добавьте следующую зависимость в ваш проект:

 ```java
 val AkkaVersion = "2.6.8"
 libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
 ```

 DSL-модули Java и Scala модулей Akka объединены в одном и том же JAR-файле. Для удобства разработки при использовании IDE, такой как Eclipse или IntelliJ, вы можете отключить автоматический импортер от предложения импорта javadsl при работе в Scala или наоборот.

 # Akka Актеры
[Модель Actor](https://en.wikipedia.org/wiki/Actor_model) обеспечивает более высокий уровень абстракции для написания параллельных и распределенных систем. Это избавляет разработчика от необходимости иметь дело с явной блокировкой и управлением потоками, облегчая написание правильных параллельных и параллельных систем. Актеры были определены в статье 1973 года Карлом Хьюиттом, но были популяризированы языком Эрланга и использовались, например, в Ericsson с большим успехом для создания высококонкурентных и надежных телекоммуникационных систем. API Akka Actors позаимствовал часть своего синтаксиса у Erlang.

## Первый пример
Если вы новичок в Akka, вы можете начать с прочтения руководства по [началу работы](https://doc.akka.io/docs/akka/current/typed/guide/introduction.html), а затем вернуться сюда, чтобы узнать больше. Мы также рекомендуем посмотреть короткое вступительное видео с [актерами Акки](https://akka.io/blog/news/2019/12/03/akka-typed-actor-intro-video).

Полезно ознакомиться с основополагающей, внешней и внутренней экосистемой ваших актеров, увидеть, что вы можете использовать и настроить по мере необходимости, см. [Системы акторов](https://doc.akka.io/docs/akka/current/general/actor-systems.html) и [ссылки на актеров, пути и адреса](https://doc.akka.io/docs/akka/current/general/addressing.html).

Как уже говорилось в [Actor Systems](https://doc.akka.io/docs/akka/current/general/actor-systems.html), актеры говорят об отправке сообщений между независимыми единицами вычислений, но как это выглядит?

Во всех следующих случаях предполагается импорт:
```java
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
```
С их помощью мы можем определить нашего первого актера, и он скажет привет!
 ![](https://doc.akka.io/docs/akka/current/typed/images/hello-world1.png)
 ```java
 public class HelloWorld extends AbstractBehavior<HelloWorld.Greet> {

  public static final class Greet {
    public final String whom;
    public final ActorRef<Greeted> replyTo;

    public Greet(String whom, ActorRef<Greeted> replyTo) {
      this.whom = whom;
      this.replyTo = replyTo;
    }
  }

  public static final class Greeted {
    public final String whom;
    public final ActorRef<Greet> from;

    public Greeted(String whom, ActorRef<Greet> from) {
      this.whom = whom;
      this.from = from;
    }
  }

  public static Behavior<Greet> create() {
    return Behaviors.setup(HelloWorld::new);
  }

  private HelloWorld(ActorContext<Greet> context) {
    super(context);
  }

  @Override
  public Receive<Greet> createReceive() {
    return newReceiveBuilder().onMessage(Greet.class, this::onGreet).build();
  }

  private Behavior<Greet> onGreet(Greet command) {
    getContext().getLog().info("Hello {}!", command.whom);
    command.replyTo.tell(new Greeted(command.whom, getContext().getSelf()));
    return this;
  }
}
 ```

Этот небольшой фрагмент кода определяет два типа сообщений: один для того, чтобы дать команду Актору приветствовать кого-то, а другой - для использования Актором, чтобы подтвердить, что он это сделал. Тип `Greet` содержит не только информацию о том, кого приветствовать, но также содержит `ActorRef`, который предоставляет отправитель сообщения, чтобы субъект `HelloWorld` мог отправить обратно подтверждающее сообщение.

Поведение актера определяется как приветствующий с помощью фабрики поведения получения. Обработка следующего сообщения затем приводит к новому поведению, которое потенциально может отличаться от этого. Состояние обновляется, возвращая новое поведение, которое содержит новое неизменное состояние. В этом случае нам не нужно обновлять какое-либо состояние, поэтому мы возвращаем это, что означает, что следующее поведение «совпадает с текущим».

Тип сообщений, обрабатываемых этим поведением, объявлен как класс Greet. Как правило, субъект обрабатывает более одного определенного типа сообщений, где все они прямо или косвенно реализуют общий интерфейс.

В последней строке мы видим, что `HelloWorld Actor` отправляет сообщение другому `Actor`, что делается с помощью метода `tell`. Это асинхронная операция, которая не блокирует поток вызывающего.

Поскольку адрес `replyTo` объявлен как тип `ActorRef <Greeted>`, компилятор будет разрешать нам только отправлять сообщения этого типа, в противном случае произойдет ошибка компилятора.

Принятые типы сообщений субъекта вместе со всеми типами ответов определяет протокол, на котором говорит этот субъект; в этом случае это простой протокол запрос-ответ, но субъекты могут при необходимости моделировать произвольно сложные протоколы. Протокол связан с поведением, которое реализует его в хорошо обернутой области - класс `HelloWorld`.

Как сказал Карл Хьюитт, один из актеров не является актером - было бы очень одиноко, если бы не с кем поговорить. Нам нужен еще один актер, который взаимодействует с `Greeter`. Давайте создадим `HelloWorldBot`, который получает ответ от `Greeter`, отправляет несколько дополнительных приветственных сообщений и собирает ответы, пока не будет достигнуто заданное максимальное количество сообщений.
![](https://doc.akka.io/docs/akka/current/typed/images/hello-world2.png)

```java
public class HelloWorldBot extends AbstractBehavior<HelloWorld.Greeted> {

  public static Behavior<HelloWorld.Greeted> create(int max) {
    return Behaviors.setup(context -> new HelloWorldBot(context, max));
  }

  private final int max;
  private int greetingCounter;

  private HelloWorldBot(ActorContext<HelloWorld.Greeted> context, int max) {
    super(context);
    this.max = max;
  }

  @Override
  public Receive<HelloWorld.Greeted> createReceive() {
    return newReceiveBuilder().onMessage(HelloWorld.Greeted.class, this::onGreeted).build();
  }

  private Behavior<HelloWorld.Greeted> onGreeted(HelloWorld.Greeted message) {
    greetingCounter++;
    getContext().getLog().info("Greeting {} for {}", greetingCounter, message.whom);
    if (greetingCounter == max) {
      return Behaviors.stopped();
    } else {
      message.from.tell(new HelloWorld.Greet(message.whom, getContext().getSelf()));
      return this;
    }
  }
}
```

Обратите внимание, как этот Actor управляет счетчиком с помощью переменной экземпляра. Нет необходимости в средствах защиты от параллелизма, таких как `synchronized` или `AtomicInteger`, поскольку экземпляр субъекта обрабатывает одно сообщение за раз.

Третий актер порождает `Greeter` и `HelloWorldBot` и начинает взаимодействие между ними.

```java
public class HelloWorldMain extends AbstractBehavior<HelloWorldMain.SayHello> {

  public static class SayHello {
    public final String name;

    public SayHello(String name) {
      this.name = name;
    }
  }

  public static Behavior<SayHello> create() {
    return Behaviors.setup(HelloWorldMain::new);
  }

  private final ActorRef<HelloWorld.Greet> greeter;

  private HelloWorldMain(ActorContext<SayHello> context) {
    super(context);
    greeter = context.spawn(HelloWorld.create(), "greeter");
  }

  @Override
  public Receive<SayHello> createReceive() {
    return newReceiveBuilder().onMessage(SayHello.class, this::onStart).build();
  }

  private Behavior<SayHello> onStart(SayHello command) {
    ActorRef<HelloWorld.Greeted> replyTo =
        getContext().spawn(HelloWorldBot.create(3), command.name);
    greeter.tell(new HelloWorld.Greet(command.name, replyTo));
    return this;
  }
}
```

Теперь мы хотим опробовать этот Actor, поэтому мы должны запустить `ActorSystem` для его размещения:

```java
final ActorSystem<HelloWorldMain.SayHello> system =
    ActorSystem.create(HelloWorldMain.create(), "hello");

system.tell(new HelloWorldMain.SayHello("World"));
system.tell(new HelloWorldMain.SayHello("Akka"));
```

Мы запускаем систему Actor из определенного поведения `HelloWorldMain` и отправляем два сообщения `SayHello`, которые запускают взаимодействие между двумя отдельными актерами `HelloWorldBot` и одним актером `Greeter`.

Приложение обычно состоит из одной системы `ActorSystem`, в которой выполняется много действующих лиц для каждой виртуальной машины Java.

Вывод консоли может выглядеть следующим образом:

```
[INFO] [03/13/2018 15:50:05.814] [hello-akka.actor.default-dispatcher-4] [akka://hello/user/greeter] Hello World!
[INFO] [03/13/2018 15:50:05.815] [hello-akka.actor.default-dispatcher-4] [akka://hello/user/greeter] Hello Akka!
[INFO] [03/13/2018 15:50:05.815] [hello-akka.actor.default-dispatcher-2] [akka://hello/user/World] Greeting 1 for World
[INFO] [03/13/2018 15:50:05.815] [hello-akka.actor.default-dispatcher-4] [akka://hello/user/Akka] Greeting 1 for Akka
[INFO] [03/13/2018 15:50:05.815] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] Hello World!
[INFO] [03/13/2018 15:50:05.815] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] Hello Akka!
[INFO] [03/13/2018 15:50:05.815] [hello-akka.actor.default-dispatcher-4] [akka://hello/user/World] Greeting 2 for World
[INFO] [03/13/2018 15:50:05.815] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] Hello World!
[INFO] [03/13/2018 15:50:05.815] [hello-akka.actor.default-dispatcher-4] [akka://hello/user/Akka] Greeting 2 for Akka
[INFO] [03/13/2018 15:50:05.816] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] Hello Akka!
[INFO] [03/13/2018 15:50:05.816] [hello-akka.actor.default-dispatcher-4] [akka://hello/user/World] Greeting 3 for World
[INFO] [03/13/2018 15:50:05.816] [hello-akka.actor.default-dispatcher-6] [akka://hello/user/Akka] Greeting 3 for Akka
```

# Более комплексные приеры

Следующий пример более реалистичен и демонстрирует несколько важных шаблонов:

+ Использование интерфейса и классов, реализующих этот интерфейс, для представления нескольких сообщений, которые может получить субъект
+ Обработка сессий с использованием дочерних актеров
+ Обработка состояния путем изменения поведения
+ Использование нескольких действующих лиц для представления различных частей протокола безопасным способом
![chat-room.png](https://doc.akka.io/docs/akka/current/typed/images/chat-room.png)

## Функциональный стиль
Сначала мы покажем этот пример в функциональном стиле, а затем тот же пример показан в [объектно-ориентированном стиле](https://doc.akka.io/docs/akka/current/typed/actors.html#object-oriented-style). Какой стиль вы выберете, зависит от вкуса, и оба стиля могут быть смешаны в зависимости от того, какой из них лучше всего подходит для конкретного актера. Соображения по поводу выбора приведены в [Руководстве по стилю](https://doc.akka.io/docs/akka/current/typed/style-guide.html#functional-versus-object-oriented-style).

Рассмотрим актера, который работает в чате: клиентские актеры могут подключиться, отправив сообщение, содержащее их псевдоним, а затем они могут публиковать сообщения. Участник чата рассылает все опубликованные сообщения всем подключенным в данный момент клиентам. Определение протокола может выглядеть следующим образом:

```java
static interface RoomCommand {}

public static final class GetSession implements RoomCommand {
  public final String screenName;
  public final ActorRef<SessionEvent> replyTo;

  public GetSession(String screenName, ActorRef<SessionEvent> replyTo) {
    this.screenName = screenName;
    this.replyTo = replyTo;
  }
}

interface SessionEvent {}

public static final class SessionGranted implements SessionEvent {
  public final ActorRef<PostMessage> handle;

  public SessionGranted(ActorRef<PostMessage> handle) {
    this.handle = handle;
  }
}

public static final class SessionDenied implements SessionEvent {
  public final String reason;

  public SessionDenied(String reason) {
    this.reason = reason;
  }
}

public static final class MessagePosted implements SessionEvent {
  public final String screenName;
  public final String message;

  public MessagePosted(String screenName, String message) {
    this.screenName = screenName;
    this.message = message;
  }
}

interface SessionCommand {}

public static final class PostMessage implements SessionCommand {
  public final String message;

  public PostMessage(String message) {
    this.message = message;
  }
}

private static final class NotifyClient implements SessionCommand {
  final MessagePosted message;

  NotifyClient(MessagePosted message) {
    this.message = message;
  }
}
```

Первоначально клиентские актеры получают доступ только к `ActorRef <GetSession>`, который позволяет им сделать первый шаг. Как только сеанс клиента установлен, он получает сообщение `SessionGranted`, которое содержит дескриптор для разблокировки следующего шага протокола, отправки сообщений. Команду `PostMessage` нужно будет отправить на этот конкретный адрес, представляющий сеанс, который был добавлен в комнату чата. Другим аспектом сеанса является то, что клиент открыл свой собственный адрес через аргумент `replyTo`, так что последующие события `MessagePosted` могут быть отправлены ему.

Это показывает, как субъекты могут выражать больше, чем просто эквивалент вызовов методов для объектов Java. Объявленные типы сообщений и их содержимое описывают полный протокол, который может включать несколько субъектов и который может развиваться в несколько этапов. Вот реализация протокола чата:

```java
public class ChatRoom {
  private static final class PublishSessionMessage implements RoomCommand {
    public final String screenName;
    public final String message;

    public PublishSessionMessage(String screenName, String message) {
      this.screenName = screenName;
      this.message = message;
    }
  }

  public static Behavior<RoomCommand> create() {
    return Behaviors.setup(
        ctx -> new ChatRoom(ctx).chatRoom(new ArrayList<ActorRef<SessionCommand>>()));
  }

  private final ActorContext<RoomCommand> context;

  private ChatRoom(ActorContext<RoomCommand> context) {
    this.context = context;
  }

  private Behavior<RoomCommand> chatRoom(List<ActorRef<SessionCommand>> sessions) {
    return Behaviors.receive(RoomCommand.class)
        .onMessage(GetSession.class, getSession -> onGetSession(sessions, getSession))
        .onMessage(PublishSessionMessage.class, pub -> onPublishSessionMessage(sessions, pub))
        .build();
  }

  private Behavior<RoomCommand> onGetSession(
      List<ActorRef<SessionCommand>> sessions, GetSession getSession)
      throws UnsupportedEncodingException {
    ActorRef<SessionEvent> client = getSession.replyTo;
    ActorRef<SessionCommand> ses =
        context.spawn(
            Session.create(context.getSelf(), getSession.screenName, client),
            URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name()));
    // narrow to only expose PostMessage
    client.tell(new SessionGranted(ses.narrow()));
    List<ActorRef<SessionCommand>> newSessions = new ArrayList<>(sessions);
    newSessions.add(ses);
    return chatRoom(newSessions);
  }

  private Behavior<RoomCommand> onPublishSessionMessage(
      List<ActorRef<SessionCommand>> sessions, PublishSessionMessage pub) {
    NotifyClient notification =
        new NotifyClient((new MessagePosted(pub.screenName, pub.message)));
    sessions.forEach(s -> s.tell(notification));
    return Behaviors.same();
  }

  static class Session {
    static Behavior<ChatRoom.SessionCommand> create(
        ActorRef<RoomCommand> room, String screenName, ActorRef<SessionEvent> client) {
      return Behaviors.receive(ChatRoom.SessionCommand.class)
          .onMessage(PostMessage.class, post -> onPostMessage(room, screenName, post))
          .onMessage(NotifyClient.class, notification -> onNotifyClient(client, notification))
          .build();
    }

    private static Behavior<SessionCommand> onPostMessage(
        ActorRef<RoomCommand> room, String screenName, PostMessage post) {
      // from client, publish to others via the room
      room.tell(new PublishSessionMessage(screenName, post.message));
      return Behaviors.same();
    }

    private static Behavior<SessionCommand> onNotifyClient(
        ActorRef<SessionEvent> client, NotifyClient notification) {
      // published from the room
      client.tell(notification.message);
      return Behaviors.same();
    }
  }
}
```

Состояние управляется изменением поведения, а не использованием каких-либо переменных.

Когда приходит новая команда `GetSession`, мы добавляем этого клиента в список с возвращаемым поведением. Затем нам также нужно создать сессионный `ActorRef`, который будет использоваться для публикации сообщений. В этом случае мы хотим создать очень простой `Actor`, который перепаковывает команду `PostMessage` в команду `PublishSessionMessage`, которая также содержит отображаемое имя.

Поведение, которое мы здесь заявляем, может обрабатывать оба подтипа `RoomCommand`. GetSession уже был объяснен, и команды `PublishSessionMessage`, поступающие из сеанса. Актеры инициируют распространение содержащегося сообщения чата для всех подключенных клиентов. Но мы не хотим давать возможность отправлять команды `PublishSessionMessage` произвольным клиентам, мы оставляем это право для внутренних участников сеанса, которые мы создаем - в противном случае клиенты могут представлять собой совершенно разные экранные имена (представьте, что протокол `GetSession` включает информацию об аутентификации для дополнительной защиты этот). Поэтому `PublishSessionMessage` имеет частную видимость и не может быть создан вне класса `ChatRoom`.

Если бы мы не заботились о защите соответствия между сеансом и отображаемым именем, мы могли бы изменить протокол так, чтобы `PostMessage` удалялся, и все клиенты просто получали `ActorRef <PublishSessionMessage>` для отправки. В этом случае сессионный субъект не понадобится, и мы можем использовать `context.getSelf ()`. В этом случае срабатывают проверки типов, потому что `ActorRef <T>` является контравариантным в своем параметре типа, что означает, что мы можем использовать `ActorRef <RoomCommand>` везде, где требуется `ActorRef <PublishSessionMessage>` - это имеет смысл, поскольку первый просто говорит на нескольких языках чем последний. Обратное было бы проблематично, поэтому передача `ActorRef <PublishSessionMessage>`, где требуется `ActorRef <RoomCommand>`, приведет к ошибке типа.

Чтобы увидеть этот чат в действии, нам нужно написать клиентский Actor, который сможет его использовать:

```java
public class Gabbler {
  public static Behavior<ChatRoom.SessionEvent> create() {
    return Behaviors.setup(ctx -> new Gabbler(ctx).behavior());
  }

  private final ActorContext<ChatRoom.SessionEvent> context;

  private Gabbler(ActorContext<ChatRoom.SessionEvent> context) {
    this.context = context;
  }

  private Behavior<ChatRoom.SessionEvent> behavior() {
    return Behaviors.receive(ChatRoom.SessionEvent.class)
        .onMessage(ChatRoom.SessionDenied.class, this::onSessionDenied)
        .onMessage(ChatRoom.SessionGranted.class, this::onSessionGranted)
        .onMessage(ChatRoom.MessagePosted.class, this::onMessagePosted)
        .build();
  }

  private Behavior<ChatRoom.SessionEvent> onSessionDenied(ChatRoom.SessionDenied message) {
    context.getLog().info("cannot start chat room session: {}", message.reason);
    return Behaviors.stopped();
  }

  private Behavior<ChatRoom.SessionEvent> onSessionGranted(ChatRoom.SessionGranted message) {
    message.handle.tell(new ChatRoom.PostMessage("Hello World!"));
    return Behaviors.same();
  }

  private Behavior<ChatRoom.SessionEvent> onMessagePosted(ChatRoom.MessagePosted message) {
    context
        .getLog()
        .info("message has been posted by '{}': {}", message.screenName, message.message);
    return Behaviors.stopped();
  }
}
```
Исходя из этого, мы можем создать актера, который будет принимать сеанс в чате, публиковать сообщения, ждать, пока оно не будет опубликовано, а затем завершать работу. Последний шаг требует способности изменить поведение, нам нужно перейти от нормального поведения при работе в завершенное состояние. Вот почему здесь мы не возвращаем то же самое, что и выше, но другое специальное значение остановлено.

Теперь, чтобы попробовать что-то новое, мы должны запустить чат и рулетку, и, конечно же, мы делаем это внутри системы `Actor`. Так как может быть только один опекун пользователя, мы могли бы либо запустить чат из `gabbler` (что нам не нужно - это усложняет его логику), либо из `gabbler` из чата (что бессмысленно), либо мы начинаем оба из третий актер - наш единственный разумный выбор:

```java
public class Main {
  public static Behavior<Void> create() {
    return Behaviors.setup(
        context -> {
          ActorRef<ChatRoom.RoomCommand> chatRoom = context.spawn(ChatRoom.create(), "chatRoom");
          ActorRef<ChatRoom.SessionEvent> gabbler = context.spawn(Gabbler.create(), "gabbler");
          context.watch(gabbler);
          chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler));

          return Behaviors.receive(Void.class)
              .onSignal(Terminated.class, sig -> Behaviors.stopped())
              .build();
        });
  }

  public static void main(String[] args) {
    ActorSystem.create(Main.create(), "ChatRoomDemo");
  }
}
```

По доброй традиции мы называем `Main Actor` таким, какой он есть, он напрямую соответствует методу `main` в традиционном приложении Java. Этот актер будет выполнять свою работу самостоятельно, нам не нужно отправлять сообщения извне, поэтому мы объявляем его типа `Void`. Актеры получают не только внешние сообщения, они также уведомляются о некоторых системных событиях, так называемых сигналах. Для того, чтобы получить доступ к ним, мы решили реализовать именно этот, используя декоратор поведения при получении. Предоставленная функция `onSignal` будет вызываться для сигналов (подклассы `Signal`) или функция `onMessage` для пользовательских сообщений.

Этот конкретный главный актер создается с помощью `Behaviors.setup`, который похож на фабрику для поведения. Создание экземпляра поведения откладывается до запуска субъекта, в отличие от `Behaviors.receive`, который создает экземпляр поведения непосредственно перед запуском субъекта. Заводская функция в настройке передает `ActorContext` в качестве параметра и может, например, использоваться для порождения дочерних акторов. Этот главный актер создает чат-комнату, и габблер и сеанс между ними инициируются, и, когда габблер закончится, мы получим событие Termination, так как оно вызвало context.watch для него. Это позволяет нам выключить систему актера: когда главный актер завершает работу, больше ничего не остается.

Поэтому после создания системы `Actor` с поведением главного актора мы можем позволить возврату основного метода, `ActorSystem` продолжит работу и JVM будет работать до тех пор, пока корневой актер не остановится.

## Объектно-ориентированный стиль
В приведенном выше примере использовался стиль функционального программирования, когда вы передаете функцию фабрике, которая затем создает поведение, для действующих лиц с состоянием это означает передачу неизменного состояния в качестве параметров и переключение на новое поведение всякий раз, когда вам нужно воздействовать на измененное состояние. Альтернативный способ выразить то же самое - более объектно-ориентированный стиль, в котором определен конкретный класс для поведения актера, и изменяемое состояние сохраняется внутри него как поля.

Какой стиль вы выберете, зависит от вкуса, и оба стиля могут быть смешаны в зависимости от того, какой из них лучше всего подходит для конкретного актера. Соображения по поводу выбора приведены в [Руководстве по стилю](https://doc.akka.io/docs/akka/current/typed/style-guide.html#functional-versus-object-oriented-style).

## AbstractBehavior API
Определение поведения актера на основе класса начинается с расширения `akka.actor.typed.javadsl.AbstractBehavior <T>`, где `T `- это тип сообщений, которые будет принимать поведение.

Давайте повторим пример чата из [более сложного примера выше](https://doc.akka.io/docs/akka/current/typed/actors.html#a-more-complex-example), но реализованного с использованием `AbstractBehavior`. Протокол взаимодействия с актером выглядит так же:

```java
tatic interface RoomCommand {}

public static final class GetSession implements RoomCommand {
  public final String screenName;
  public final ActorRef<SessionEvent> replyTo;

  public GetSession(String screenName, ActorRef<SessionEvent> replyTo) {
    this.screenName = screenName;
    this.replyTo = replyTo;
  }
}

static interface SessionEvent {}

public static final class SessionGranted implements SessionEvent {
  public final ActorRef<PostMessage> handle;

  public SessionGranted(ActorRef<PostMessage> handle) {
    this.handle = handle;
  }
}

public static final class SessionDenied implements SessionEvent {
  public final String reason;

  public SessionDenied(String reason) {
    this.reason = reason;
  }
}

public static final class MessagePosted implements SessionEvent {
  public final String screenName;
  public final String message;

  public MessagePosted(String screenName, String message) {
    this.screenName = screenName;
    this.message = message;
  }
}

static interface SessionCommand {}

public static final class PostMessage implements SessionCommand {
  public final String message;

  public PostMessage(String message) {
    this.message = message;
  }
}

private static final class NotifyClient implements SessionCommand {
  final MessagePosted message;

  NotifyClient(MessagePosted message) {
    this.message = message;
  }
}
```
Первоначально клиентские актеры получают доступ только к `ActorRef <GetSession>`, который позволяет им сделать первый шаг. Как только сеанс клиента установлен, он получает сообщение `SessionGranted`, которое содержит дескриптор для разблокировки следующего шага протокола, отправки сообщений. Команду `PostMessage` нужно будет отправить на этот конкретный адрес, представляющий сеанс, который был добавлен в комнату чата. Другим аспектом сеанса является то, что клиент открыл свой собственный адрес через аргумент `replyTo`, так что последующие события `MessagePosted` могут быть отправлены ему.

Это показывает, как субъекты могут выражать больше, чем просто эквивалент вызовов методов для объектов Java. Объявленные типы сообщений и их содержимое описывают полный протокол, который может включать несколько субъектов и который может развиваться в несколько этапов. Вот реализация `AbstractBehavior` протокола чата:

```java
public class ChatRoom {
  private static final class PublishSessionMessage implements RoomCommand {
    public final String screenName;
    public final String message;

    public PublishSessionMessage(String screenName, String message) {
      this.screenName = screenName;
      this.message = message;
    }
  }

  public static Behavior<RoomCommand> create() {
    return Behaviors.setup(ChatRoomBehavior::new);
  }

  public static class ChatRoomBehavior extends AbstractBehavior<RoomCommand> {
    final List<ActorRef<SessionCommand>> sessions = new ArrayList<>();

    private ChatRoomBehavior(ActorContext<RoomCommand> context) {
      super(context);
    }

    @Override
    public Receive<RoomCommand> createReceive() {
      ReceiveBuilder<RoomCommand> builder = newReceiveBuilder();

      builder.onMessage(GetSession.class, this::onGetSession);
      builder.onMessage(PublishSessionMessage.class, this::onPublishSessionMessage);

      return builder.build();
    }

    private Behavior<RoomCommand> onGetSession(GetSession getSession)
        throws UnsupportedEncodingException {
      ActorRef<SessionEvent> client = getSession.replyTo;
      ActorRef<SessionCommand> ses =
          getContext()
              .spawn(
                  SessionBehavior.create(getContext().getSelf(), getSession.screenName, client),
                  URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name()));
      // narrow to only expose PostMessage
      client.tell(new SessionGranted(ses.narrow()));
      sessions.add(ses);
      return this;
    }

    private Behavior<RoomCommand> onPublishSessionMessage(PublishSessionMessage pub) {
      NotifyClient notification =
          new NotifyClient((new MessagePosted(pub.screenName, pub.message)));
      sessions.forEach(s -> s.tell(notification));
      return this;
    }
  }

  static class SessionBehavior extends AbstractBehavior<ChatRoom.SessionCommand> {
    private final ActorRef<RoomCommand> room;
    private final String screenName;
    private final ActorRef<SessionEvent> client;

    public static Behavior<ChatRoom.SessionCommand> create(
        ActorRef<RoomCommand> room, String screenName, ActorRef<SessionEvent> client) {
      return Behaviors.setup(context -> new SessionBehavior(context, room, screenName, client));
    }

    private SessionBehavior(
        ActorContext<ChatRoom.SessionCommand> context,
        ActorRef<RoomCommand> room,
        String screenName,
        ActorRef<SessionEvent> client) {
      super(context);
      this.room = room;
      this.screenName = screenName;
      this.client = client;
    }

    @Override
    public Receive<SessionCommand> createReceive() {
      return newReceiveBuilder()
          .onMessage(PostMessage.class, this::onPostMessage)
          .onMessage(NotifyClient.class, this::onNotifyClient)
          .build();
    }

    private Behavior<SessionCommand> onPostMessage(PostMessage post) {
      // from client, publish to others via the room
      room.tell(new PublishSessionMessage(screenName, post.message));
      return Behaviors.same();
    }

    private Behavior<SessionCommand> onNotifyClient(NotifyClient notification) {
      // published from the room
      client.tell(notification.message);
      return Behaviors.same();
    }
  }
}
```
Состояние управляется через поля в классе, как и в обычном объектно-ориентированном классе. Поскольку состояние изменчиво, мы никогда не возвращаем поведение, отличное от логики сообщения, но можем вернуть сам экземпляр `(this) AbstractBehavior` в качестве поведения, которое будет использоваться для обработки следующего поступающего сообщения. Мы также могли бы вернуть `Behavior.same` для достижения тем же.

В этом примере мы создаем отдельные операторы для создания построителя поведения, но он также возвращает самого построителя с каждого шага, поэтому также возможен более свободный стиль определения поведения. Что вы предпочитаете, зависит от того, насколько большой набор сообщений принимает актер.

Также возможно вернуть новый другой `AbstractBehavior`, например, чтобы представить другое состояние в автомате конечных состояний `(FSM)`, или использовать одну из фабрик функционального поведения, чтобы объединить объект, ориентированный с функциональным стилем, для разных частей жизненного цикла. того же поведения актера.

Когда приходит новая команда `GetSession`, мы добавляем этого клиента в список текущих сеансов. Затем нам также нужно создать сессионный `ActorRef`, который будет использоваться для публикации сообщений. В этом случае мы хотим создать очень простой Actor, который перепаковывает команду `PostMessage` в команду `PublishSessionMessage`, которая также содержит отображаемое имя.

Чтобы реализовать логику, в которой мы порождаем дочерний элемент для сеанса, нам нужен доступ к `ActorContext`. Это внедряется как параметр конструктора при создании поведения, обратите внимание, как мы комбинируем `AbstractBehavior` с `Behaviors.setup`, чтобы сделать это в методе создания фабрики.

Поведение, которое мы здесь заявляем, может обрабатывать оба подтипа `RoomCommand. GetSession` уже был объяснен, и команды `PublishSessionMessage`, поступающие из сеанса. Актеры инициируют распространение содержащегося сообщения чата для всех подключенных клиентов. Но мы не хотим давать возможность отправлять команды `PublishSessionMessage` произвольным клиентам, мы оставляем это право для внутренних участников сеанса, которые мы создаем - в противном случае клиенты могут представлять собой совершенно разные экранные имена (представьте, что протокол `GetSession` включает информацию об аутентификации для дополнительной защиты этот). Поэтому `PublishSessionMessage` имеет частную видимость и не может быть создан вне класса `ChatRoom`.

Если бы мы не заботились о защите соответствия между сеансом и отображаемым именем, мы могли бы изменить протокол так, чтобы PostMessage удалялся, и все клиенты просто получали `ActorRef <PublishSessionMessage>` для отправки. В этом случае сессионный субъект не понадобится, и мы можем использовать `context.getSelf ()`. В этом случае срабатывают проверки типов, потому что `ActorRef <T>` является контравариантным в своем параметре типа, что означает, что мы можем использовать `ActorRef <RoomCommand>` везде, где требуется `ActorRef <PublishSessionMessage>` - это имеет смысл, поскольку первый просто говорит на нескольких языках чем последний. Обратное было бы проблематично, поэтому передача `ActorRef <PublishSessionMessage>`, где требуется `ActorRef <RoomCommand>`, приведет к ошибке типа.

Чтобы увидеть этот чат в действии, нам нужно написать клиентский Actor, который сможет его использовать:

```java
public class Gabbler extends AbstractBehavior<ChatRoom.SessionEvent> {
  public static Behavior<ChatRoom.SessionEvent> create() {
    return Behaviors.setup(Gabbler::new);
  }

  private Gabbler(ActorContext<ChatRoom.SessionEvent> context) {
    super(context);
  }

  @Override
  public Receive<ChatRoom.SessionEvent> createReceive() {
    ReceiveBuilder<ChatRoom.SessionEvent> builder = newReceiveBuilder();
    return builder
        .onMessage(ChatRoom.SessionDenied.class, this::onSessionDenied)
        .onMessage(ChatRoom.SessionGranted.class, this::onSessionGranted)
        .onMessage(ChatRoom.MessagePosted.class, this::onMessagePosted)
        .build();
  }

  private Behavior<ChatRoom.SessionEvent> onSessionDenied(ChatRoom.SessionDenied message) {
    getContext().getLog().info("cannot start chat room session: {}", message.reason);
    return Behaviors.stopped();
  }

  private Behavior<ChatRoom.SessionEvent> onSessionGranted(ChatRoom.SessionGranted message) {
    message.handle.tell(new ChatRoom.PostMessage("Hello World!"));
    return Behaviors.same();
  }

  private Behavior<ChatRoom.SessionEvent> onMessagePosted(ChatRoom.MessagePosted message) {
    getContext()
        .getLog()
        .info("message has been posted by '{}': {}", message.screenName, message.message);
    return Behaviors.stopped();
  }
}
```

Теперь, чтобы попробовать что-то новое, мы должны запустить чат и рулетку, и, конечно же, мы делаем это внутри системы `Actor`. Так как может быть только один опекун пользователя, мы могли бы либо запустить чат из `gabbler` (что нам не нужно - это усложняет его логику), либо из `gabbler` из чата (что бессмысленно), либо мы начинаем оба из третий актер - наш единственный разумный выбор:

```java
public class Main {
  public static Behavior<Void> create() {
    return Behaviors.setup(
        context -> {
          ActorRef<ChatRoom.RoomCommand> chatRoom = context.spawn(ChatRoom.create(), "chatRoom");
          ActorRef<ChatRoom.SessionEvent> gabbler = context.spawn(Gabbler.create(), "gabbler");
          context.watch(gabbler);
          chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler));

          return Behaviors.receive(Void.class)
              .onSignal(Terminated.class, sig -> Behaviors.stopped())
              .build();
        });
  }

  public static void main(String[] args) {
    ActorSystem.create(Main.create(), "ChatRoomDemo");
  }
}
```
По доброй традиции мы называем `Main` `Actor` таким, какой он есть, он напрямую соответствует методу main в традиционном приложении Java. Этот актер будет выполнять свою работу самостоятельно, нам не нужно отправлять сообщения извне, поэтому мы объявляем его типа `Void`. Актеры получают не только внешние сообщения, они также уведомляются о некоторых системных событиях, так называемых сигналах. Для того, чтобы получить доступ к ним, мы решили реализовать именно этот, используя декоратор поведения при получении. Предоставленная функция `onSigna`l будет вызываться для сигналов (подклассы `Signal`) или функция onMessage для пользовательских сообщений.

Этот конкретный главный актер создается с помощью `Behaviors.setup`, который похож на фабрику для поведения. Создание экземпляра поведения откладывается до запуска субъекта, в отличие от `Behaviors.receive`, который создает экземпляр поведения непосредственно перед запуском субъекта. Заводская функция в настройке передает `ActorContext` в качестве параметра и может, например, использоваться для порождения дочерних акторов. Этот главный актер создает чат-комнату, и габблер и сеанс между ними инициируются, и, когда габблер закончится, мы получим событие Termination, так как оно вызвало `context.watch` для него. Это позволяет нам выключить систему актера: когда главный актер завершает работу, больше ничего не остается.

Поэтому после создания системы `Actor` с поведением главного актора мы можем позволить возврату основного метода, `ActorSystem` продолжит работу и JVM будет работать до тех пор, пока корневой актер не остановится.