package com.softwaremill.sandbox.application

import kamon.metric.{EntityRecorderFactory, GenericEntityRecorder}
import kamon.metric.instrument.{InstrumentFactory, Time}

class ActorMetrics(instrumentFactory: InstrumentFactory) extends GenericEntityRecorder(instrumentFactory) {
  val timeInMailbox = histogram("custom-time-in-mailbox", Time.Nanoseconds)
  val processingTime = histogram("custom-processing-time", Time.Nanoseconds)
  val mailboxSize = minMaxCounter("custom-mailbox-size")
  val errors = counter("custom-errors")
}

object ActorMetrics extends EntityRecorderFactory[ActorMetrics] {
  def category: String = "custom_ActorMetrics"
  def createRecorder(instrumentFactory: InstrumentFactory): ActorMetrics = new ActorMetrics(instrumentFactory)
}
