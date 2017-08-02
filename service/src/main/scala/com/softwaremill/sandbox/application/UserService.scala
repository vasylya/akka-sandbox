package com.softwaremill.sandbox.application

import java.util.UUID

import akka.actor.ActorSystem
import com.softwaremill.sandbox.application.UserActor.UserRegion

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import akka.pattern._
import akka.util.Timeout
import kamon.Kamon
import kamon.trace.Tracer

import scala.concurrent.duration._

class UserService(userRegion: UserRegion)(implicit executionContext: ExecutionContext, actorSystem: ActorSystem) {

  import UserService._

  private implicit val timeout = Timeout(10.seconds)
  private val random = new Random()

  def createUser(uuid: UUID): Future[String] = {
//    customManagedActorMetrics.mailboxSize.increment()
//    customManagedActorMetrics.processingTime.record(random.nextInt(500))
//    customManagedActorMetrics.mailboxSize.decrement()
    val uniqueName = "andrzej" + random.nextInt(100)
    (userRegion ? UserActor.CreateUser(uuid.toString, uniqueName)).mapTo[String]
  }

  def getUser(uuid: UUID): Future[Option[String]] = {
//    getUserCounter.increment()
//    getUserMMCounter.increment(10)
//    getUserHistogram.record(700L)
//    getUserTaggedHistogram.record(800L)
//    customManagedActorMetrics.mailboxSize.increment()
//    customManagedActorMetrics.processingTime.record(random.nextInt(500))
//    customManagedActorMetrics.mailboxSize.decrement()
//    Tracer.currentContext
//      .withNewAsyncSegment("FUTURE_some-cool-section", "business-logic", "kamon") {
//      }
    (userRegion ? UserActor.GetUser(uuid.toString)).mapTo[Option[String]]
  }
}

object UserService {
//  val getUserCounter = Kamon.metrics.counter("get-user-counter")
//  val getUserMMCounter = Kamon.metrics.minMaxCounter("get-user-mm-counter", refreshInterval = 500 milliseconds)
//  val getUserHistogram = Kamon.metrics.histogram("get-user-histogram")
//  val getUserTaggedHistogram = Kamon.metrics.histogram("get-user-tagged-histogram", tags = Map("algorithm" -> "X"))
//  val customManagedActorMetrics = Kamon.metrics.entity(ActorMetrics, "my-managed-actor")

  //shouldTrack(customManagedActorMetrics)
}
