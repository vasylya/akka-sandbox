package com.softwaremill.sandbox.api.http

import java.util.UUID

import akka.http.scaladsl.server.Directives.{complete, onSuccess, pathEnd, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import com.softwaremill.sandbox.application.UserService
import com.typesafe.scalalogging.LazyLogging
import kamon.Kamon
import kamon.akka.http.KamonTraceDirectives
import kamon.trace.Tracer

import scala.concurrent.{ExecutionContext, Future}

class UserController(userService: UserService)(implicit executionContext: ExecutionContext) extends KamonTraceDirectives with LazyLogging {

  import UserController._

  def routes: Route = pathPrefix("user") {
    pathEnd {
      post {
        logger.debug(s"Tracer.currentContext.name = ${Tracer.currentContext.name}")
        val time = System.currentTimeMillis()
        val res = traceName("user-creation") {
          val uuid = UUID.randomUUID()
          logger.debug(s"processing user $uuid creation request [token ${Tracer.currentContext.token}]")
          Tracer.currentContext.withNewSegment("create-user_NO_async_segment", "create-user", "request") {
            Thread.sleep(1000)
            Tracer.currentContext.withNewAsyncSegment("create-user_async_segment", "create-user", "request") {
              Future {
                Thread.sleep(5000)
              }
            }
          }
          onSuccess(userService.createUser(uuid)) {
            case name: String =>
              Thread.sleep(7000)
              logger.debug(s"creating user $uuid finished [token ${Tracer.currentContext.token}]")
              complete(201, s"user $uuid created")
          }
        }
        checkCreateUserHistogram.record(System.currentTimeMillis() - time)
        res
      }
    } ~
      pathPrefix(JavaUUID) { uuid =>
        pathEnd {
          get {
            logger.debug(s"Tracer.currentContext.name = ${Tracer.currentContext.name}")
            val time = System.currentTimeMillis()
            val res = traceName("get-user") {
              Tracer.currentContext.withNewSegment("get-user_NO_async_segment", "get-user", "request") {
                Thread.sleep(1000)
                Tracer.currentContext.withNewAsyncSegment("get-user_async_segment", "get-user", "request") {
                  Future {
                    Thread.sleep(5000)
                  }
                }
              }
              onSuccess(userService.getUser(uuid)) {
                case Some(userName) =>
                  Thread.sleep(7000)
                  logger.debug(s"getting user $uuid finished [token ${Tracer.currentContext.token}]")
                  complete(200, s"user $uuid found, name is: $userName")
                case None =>
                  Thread.sleep(7000)
                  logger.debug(s"user $uuid not found [token ${Tracer.currentContext.token}]")
                  complete(404, s"user $uuid not found")
              }
            }
            checkGetUserHistogram.record(System.currentTimeMillis() - time)
            res
          }
        }
      }
  }
}

object UserController {
  val checkCreateUserHistogram = Kamon.metrics.histogram("check-create-user-histogram")
  val checkGetUserHistogram = Kamon.metrics.histogram("check-get-user-histogram")
}
