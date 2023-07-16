package mlb

import zio._
import zio.jdbc._
import zio.http._
import mlb.Endpoints.endpoints
import mlb.Database.*
import Constants.*
import com.github.tototoshi.csv._
import zio.stream.ZStream
import java.time.LocalDate

object MlbApi extends ZIOAppDefault {

  val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
    ZLayer.succeed(ZConnectionPoolConfig.default)

  val properties: Map[String, String] = Map(
    "user" -> "sa",
    "password" -> ""
  )

  val connectionPool
      : ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool] =
    ZConnectionPool.h2mem(
      database = "testdb",
      props = properties
    )

  val app: ZIO[ZConnectionPool & Server, Throwable, Unit] = for {
    conn: Unit <- create
    source: CSVReader <- ZIO.succeed(
      CSVReader.open(new java.io.File(Elop1))
    )
    stream: Unit <- ZStream
      .fromIterator[Map[String, String]](source.iteratorWithHeaders)
      .map[Game](CsvParser.parseGame)
      .grouped(1000)
      .foreach(chunk => insertRows(chunk.toList))
    _ <- ZIO.succeed(source.close())
    _ <- Server.serve(endpoints)
  } yield ()
  override def run: ZIO[Any, Throwable, Unit] =
    app.provide(createZIOPoolConfig >>> connectionPool, Server.default)
}
