package org.actionsl.utils

import org.apache.log4j.{ Logger, ConsoleAppender }

trait Logging {
  val loggerName = this.getClass.getName
  lazy val log = Logger.getLogger(loggerName)
}
