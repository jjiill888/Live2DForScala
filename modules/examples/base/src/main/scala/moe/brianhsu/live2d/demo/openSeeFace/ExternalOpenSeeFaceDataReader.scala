package moe.brianhsu.live2d.demo.openSeeFace

import moe.brianhsu.live2d.adapter.gateway.openSeeFace.UDPOpenSeeFaceDataReader
import moe.brianhsu.live2d.boundary.gateway.openSeeFace.OpenSeeFaceDataReader
import moe.brianhsu.live2d.enitiy.openSeeFace.OpenSeeFaceData
import java.lang.ProcessBuilder.Redirect
import scala.concurrent.{ExecutionContext, Future}

import scala.util.Try

object ExternalOpenSeeFaceDataReader {
  def apply(command: String, hostname: String, port: Int, onDataRead: OpenSeeFaceData => Unit): ExternalOpenSeeFaceDataReader = {

    val builder = new ProcessBuilder(command.split(" "):_*)
      .redirectOutput(Redirect.DISCARD)
      .redirectError(Redirect.DISCARD)
    val process = builder.start()

    new ExternalOpenSeeFaceDataReader(process, hostname, port, onDataRead)
  }
   def startAsync(command: String, hostname: String, port: Int, onDataRead: OpenSeeFaceData => Unit)
                (implicit ec: ExecutionContext): Future[ExternalOpenSeeFaceDataReader] = Future {
    val reader = apply(command, hostname, port, onDataRead)
    reader.ensureStarted().get
  }
}

class ExternalOpenSeeFaceDataReader(process: Process, hostname: String, port: Int, onDataRead: OpenSeeFaceData => Unit) extends OpenSeeFaceDataReader {

  private val udpDataReader = new UDPOpenSeeFaceDataReader(hostname, port, 10)

  def isProcessAlive: Boolean = process.isAlive

  def ensureStarted(): Try[ExternalOpenSeeFaceDataReader] = Try {
    var count = 0
    var isFail = false

    while (!isFail && count < 10) {
      isFail = Try(process.exitValue).map(x => x != 0 && x != 124).getOrElse(false)
      count += 1
      Thread.sleep(50)
    }

    if (isFail) {
      throw new Exception(s"Cannot start OpenSeeFace, exitValue=${process.exitValue()}")
    }

    this
  }

  override def open(): Unit = {
    this.udpDataReader.open()
  }

  override def readData(): Try[OpenSeeFaceData] = {
    val data = this.udpDataReader.readData()
    data.foreach(onDataRead)
    data
  }

  override def close(): Unit = {
    if (process.isAlive) {
      process.destroy()
    }

    this.udpDataReader.close()
  }
  
}
