package mlb

import zio._
import zio.jdbc._
import zio.http._
import com.github.tototoshi.csv._

object MlbApi extends ZIOAppDefault {

  // Define a case class to represent each row in the CSV
  case class MLBData(date: String, season: String, team1 : String, team2: String, elo1Pre: Float, elo2Pre: Float, eloProb1: Float, eloProb2: Float, rating1Pre: Float, rating2Pre: Float, ratingProb1: Float, ratingProb2: Float)

  // Read the CSV file
  val reader = CSVReader.open(new java.io.File("C:/Users/julie/Downloads/mlb-elo/mlb-elo/mlb_elo.csv"))

  // Parse the CSV data into a list of MLBData objects
  val mlbDataList = reader
    .allWithHeaders()
    .map(row => MLBData(row("date"), row("season"), row("team1"), row("team2"), row("elo1_pre").toFloat, row("elo2_pre").toFloat, row("elo_prob1").toFloat, row("elo_prob2").toFloat, row("rating1_pre").toFloat, row("rating2_pre").toFloat, row("rating_prob1").toFloat, row("rating_prob2").toFloat))

  // Close the CSV reader
  reader.close()

  // Print the parsed data
  //mlbDataList.foreach(println)

  val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
    ZLayer.succeed(ZConnectionPoolConfig.default)

  val properties: Map[String, String] = Map(
    "user" -> "sa",
    "password" -> ""
  )

  val connectionPool : ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool] =
    ZConnectionPool.h2mem(
      database = "testdb",
      props = properties
    )

  val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"""
        CREATE TABLE IF NOT EXISTS teams (
          id SERIAL PRIMARY KEY,
          name VARCHAR(255),
          location VARCHAR(255),
          logo_url VARCHAR(255)
        );
      """
    )
  }

  val insertRows: ZIO[ZConnectionPool, Throwable, UpdateResult] = transaction {
    insert(
      sql"""
        INSERT INTO teams (name, location, logo_url)
        VALUES ('Yankees', 'New York', 'https://upload.wikimedia.org/wikipedia/en/thumb/2/25/NewYorkYankees_PrimaryLogo.svg/1200px-NewYorkYankees_PrimaryLogo.svg.png');

        INSERT INTO teams (name, location, logo_url)
        VALUES ('Red Sox', 'Boston', 'https://upload.wikimedia.org/wikipedia/en/thumb/6/6d/RedSoxPrimary_HangingSocks.svg/1200px-RedSoxPrimary_HangingSocks.svg.png');
      """
    )
  }

  val endpoints: App[Any] =
    Http
      .collect[Request] {
        case Method.GET -> Root / "init" => Response.text("Hello World!")
      /*  case Method.GET -> Root / "games" => ???
        case Method.GET -> Root / "predict" / "game" / gameId => ???*/
      }
      .withDefaultErrorResponse

  val app: ZIO[ZConnectionPool & Server, Throwable, Unit] = for {
    conn <- create *> insertRows
    _ <- Server.serve(endpoints)
  } yield ()
   
  override def run: ZIO[Any, Throwable, Unit] =
    app.provide(createZIOPoolConfig >>> connectionPool, Server.default)
 
}
