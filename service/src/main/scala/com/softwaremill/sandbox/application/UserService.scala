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
    createUserCounter.increment()
    createUserMMCounter.increment(20)
    createUserHistogram.record(900L)
    createUserTaggedHistogram.record(1800L)
    customManagedActorMetrics.mailboxSize.increment()
    customManagedActorMetrics.processingTime.record(random.nextInt(500))
    customManagedActorMetrics.mailboxSize.decrement()
    contextTest
    val uniqueName = "unique_user_" + random.nextInt(100)
    Tracer.currentContext.withNewAsyncSegment("Future_Ask_CreateUser", "business-logic", "UserService") {
      (userRegion ? UserActor.CreateUser(uuid.toString, uniqueName)).mapTo[String]
    }

  }

  def getUser(uuid: UUID): Future[Option[String]] = {
    getUserCounter.increment()
    getUserMMCounter.increment(10)
    getUserHistogram.record(700L)
    getUserTaggedHistogram.record(800L)
    customManagedActorMetrics.mailboxSize.increment()
    customManagedActorMetrics.processingTime.record(random.nextInt(500))
    customManagedActorMetrics.mailboxSize.decrement()
    Tracer.currentContext
      .withNewAsyncSegment("Future_Ask_GetUser", "business-logic", "UserService") {
        (userRegion ? UserActor.GetUser(uuid.toString))
      }
      .mapTo[Option[String]]
  }

  private def contextTest: Unit = {
    Tracer.withNewContext("1_autoFinish_false") {
      Thread.sleep(random.nextInt(100))
      Tracer.currentContext.withNewSegment("test_segment", "autoFinish_false", "test") {
        Thread.sleep(random.nextInt(200))
      }
    }
    Tracer.withNewContext("2_autoFinish_true", autoFinish = true) {
      Thread.sleep(random.nextInt(100))
      Tracer.currentContext.withNewSegment("test_segment", "autoFinish_true", "test") {
        Thread.sleep(random.nextInt(200))
      }
    }
    Tracer.withNewContext("3_autoFinish_false_outer") {
      Thread.sleep(random.nextInt(100))
      Tracer.withNewContext("3_autoFinish_true_inner", autoFinish = true) {
        Thread.sleep(random.nextInt(200))
      }
    }
    Tracer.withNewContext("4_autoFinish_true_outer", autoFinish = true) {
      Thread.sleep(random.nextInt(100))
      Tracer.withNewContext("4_autoFinish_false_inner") {
        Thread.sleep(random.nextInt(200))
      }
    }
    Tracer.withNewContext("5_autoFinish_false_outer_rename") {
      Thread.sleep(random.nextInt(100))
      Tracer.currentContext.rename("5_autoFinish_false_renamed")
      Thread.sleep(random.nextInt(200))
    }
  }
}

object UserService {
  val getUserCounter = Kamon.metrics.counter("get-user-counter")
  val getUserMMCounter = Kamon.metrics.minMaxCounter("get-user-mm-counter", refreshInterval = 500 milliseconds)
  val getUserHistogram = Kamon.metrics.histogram("get-user-histogram")
  val getUserTaggedHistogram = Kamon.metrics.histogram("get-user-tagged-histogram", tags = Map("algorithm" -> "X"))
  val createUserCounter = Kamon.metrics.counter("create-user-counter")
  val createUserMMCounter = Kamon.metrics.minMaxCounter("create-user-mm-counter", refreshInterval = 500 milliseconds)
  val createUserHistogram = Kamon.metrics.histogram("create-user-histogram")
  val createUserTaggedHistogram = Kamon.metrics.histogram("create-user-tagged-histogram", tags = Map("algorithm" -> "X"))
  val customManagedActorMetrics = Kamon.metrics.entity(ActorMetrics, "custom-managed-actor")
}
