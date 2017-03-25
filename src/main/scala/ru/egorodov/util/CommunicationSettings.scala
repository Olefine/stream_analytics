package ru.egorodov.util

import com.typesafe.config.ConfigFactory

object CommunicationSettings {
  def masterPort: Int = ConfigFactory.load().getNumber("application.master.incoming-port").intValue()
  def masterDuration: Int = ConfigFactory.load().getNumber("application.master.window-duration-seconds").intValue()

  def monitoringPort: Int = ConfigFactory.load().getNumber("application.monitoring.incoming-port").intValue()
  def monitoringDuration: Int = ConfigFactory.load().getNumber("application.monitoring.window-duration-seconds").intValue()
}
