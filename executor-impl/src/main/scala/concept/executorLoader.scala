package concept

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents


abstract class ExecutorApplication(context: LagomApplicationContext)
  extends LagomApplication(context) with AhcWSComponents {
  override lazy val lagomServer: LagomServer = serverFor[ExecutionService](wire[ExecutionServiceImpl])
}

class ExecutorLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ExecutorApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ExecutorApplication(context) with LagomDevModeComponents

}
