package com.softwaremill.sandbox.application

import kamon.metric.{EntityRecorderFactory, GenericEntityRecorder}
import kamon.metric.instrument.{InstrumentFactory, Time}

class ActorMetrics(instrumentFactory: InstrumentFactory) extends GenericEntityRecorder(instrumentFactory) {
  val timeInMailbox = histogram("time-in-mailbox", Time.Nanoseconds)
  val processingTime = histogram("processing-time", Time.Nanoseconds)
  val mailboxSize = minMaxCounter("mailbox-size")
  val errors = counter("errors")
}

object ActorMetrics extends EntityRecorderFactory[ActorMetrics] {
  def category: String = "custom_ActorMetrics"
  def createRecorder(instrumentFactory: InstrumentFactory): ActorMetrics = new ActorMetrics(instrumentFactory)
}
