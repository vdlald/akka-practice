package com.practice.useakka

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Writer {
  sealed trait Command
  final case class WriteMessage(msg: String) extends Command

  def apply(mail: ActorRef[Mail.Command]): Behavior[Command] = Behaviors.setup(context => new Writer(context, mail))
}

class Writer(context: ActorContext[Writer.Command], mail: ActorRef[Mail.Command])
  extends AbstractBehavior[Writer.Command](context) {

  import Writer._

  override def onMessage(command: Command): Behavior[Command] = command match {
    case WriteMessage(msg) =>
      mail.tell(Mail.PushMessage(msg))
      context.log.info("Pushed message: {}", msg)
      this
  }
}