package zzz.akka.avionics

import akka.actor.{Terminated, ActorRef, Actor}
import zzz.akka.avionics.Plane.GiveMeControl

object Pilots {

  case object ReadyToGo

  case object RelinquishControl

  class Pilot(plane: ActorRef,
              autopilot: ActorRef,
              var controls: ActorRef,
              altimeter: ActorRef) extends Actor {

    private var copilot: ActorRef = context.system.deadLetters

    private val copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")

    def receive = {

      case ReadyToGo =>
        copilot = context.actorFor("../" + copilotName)
    }
  }

  class Copilot(plane: ActorRef,
                autopilot: ActorRef,
                altimeter: ActorRef) extends Actor {

    private var controls: ActorRef = context.system.deadLetters
    private var pilot: ActorRef = context.system.deadLetters

    private val pilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.pilotName")

    def receive = {
      case ReadyToGo =>
        pilot = context.actorFor("../" + pilotName)
        context.watch(pilot)

      case Terminated(_) =>
        plane ! GiveMeControl
    }
  }

  class Autopilot extends Actor {

    def receive = {
      case _ =>
    }

  }

  trait PilotProvider {

    def newPilot(plane: ActorRef,
                 autopilot: ActorRef,
                 controls: ActorRef,
                 altimeter: ActorRef): Actor =
      new Pilot(plane, autopilot, controls, altimeter)

    def newCopilot(plane: ActorRef,
                   autopilot: ActorRef,
                   altimeter: ActorRef): Actor =
      new Copilot(plane, autopilot, altimeter)

    def newAutopilot: Actor = new Autopilot
  }

}
