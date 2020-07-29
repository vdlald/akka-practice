package com.practice.useakka

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable

object Mail {
  sealed trait Command
  final case class PushMessage(message: String) extends Command
  final case class PopMessage(replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class MessageRespond(message: String) extends Response

  def apply(): Behavior[Command] = Behaviors.setup(context => new Mail(context))

}

class Mail(context: ActorContext[Mail.Command]) extends AbstractBehavior[Mail.Command](context){
  import Mail._

  private var messages = mutable.Queue[String]()

  override def onMessage(command: Mail.Command): Behavior[Mail.Command] = command match {
    case PushMessage(message) =>
      messages += message
      this
    case PopMessage(replyTo) =>
      replyTo.tell(MessageRespond(messages.dequeue))
      this
  }
}
