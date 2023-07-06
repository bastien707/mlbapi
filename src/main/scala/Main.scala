package mlb

import zio._
import zio.jdbc._
import zio.http._
import com.github.tototoshi.csv._
import zio.stream.ZStream

object MlbApi extends ZIOAppDefault {

  // Define a case class to represent each row in the CSV
  case class MLBData(
      date: String,
      season: String,
      team1: String,
      team2: String,
      elo1Pre: Float,
      elo2Pre: Float,
      eloProb1: Float,
      eloProb2: Float,
      rating1Pre: Float,
      rating2Pre: Float,
      ratingProb1: Float,
      ratingProb2: Float
  )

  // Read the CSV file
  val reader = CSVReader.open(
    new java.io.File(
      "/Users/bastien/Dev/EFREI/M1/S8/functionalProgramming/mlb-elo/mlb_elo_latest.csv"
    )
  )

  // Parse the CSV data into a list of MLBData objects
  val mlbDataList = reader
    .allWithHeaders()
    .map(row =>
      MLBData(
        row("date"),
        row("season"),
        row("team1"),
        row("team2"),
        row("elo1_pre").toFloat,
        row("elo2_pre").toFloat,
        row("elo_prob1").toFloat,
        row("elo_prob2").toFloat,
        row("rating1_pre").toFloat,
        row("rating2_pre").toFloat,
        row("rating_prob1").toFloat,
        row("rating_prob2").toFloat
      )
    )
  // Close the CSV reader
  reader.close()

  val Teams =
    mlbDataList.foldLeft(Set.empty[String]) { (distinctTeams, row) =>
      distinctTeams + row.team1
    }

  val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"""
        CREATE TABLE IF NOT EXISTS teams (
          id SERIAL PRIMARY KEY,
          name VARCHAR(255)
        );
      """
    )
  }

  val insertTeams: ZIO[ZConnectionPool, Throwable, UpdateResult] = transaction {
    ZIO
      .collectAll(
        Teams.map { team =>
          insert(
            sql"""
            INSERT INTO teams (name)
            VALUES ($team)
          """
          )
        }
      )
      .map(_.headOption.getOrElse(throw new Exception("No rows inserted")))
  }

  val countTeams: ZIO[ZConnectionPool, Throwable, Option[String]] =
    transaction {
      selectOne(
        sql"SELECT COUNT(*) FROM teams".as[String]
      )
    }

  val selectTeams: ZIO[ZConnectionPool, Throwable, zio.Chunk[String]] =
    transaction {
      selectAll(
        sql"SELECT name FROM teams".as[String]
      )
    }

  def getTeamInformation(teamName: String): ZIO[ZConnectionPool, Throwable, Option[String]] = transaction {
    selectOne(
      sql"""
        SELECT name FROM teams WHERE name = $teamName
      """.as[String]
    )
  }

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

  val endpoints: App[ZConnectionPool] =
    Http
      .collectZIO[Request] {

        case Method.GET -> Root / "count" =>
          for {
            count: Option[String] <- countTeams
            res: Response = count match
              case Some(c) => Response.text(s"${c} teams")
              case None    => Response.text("No team in historical data")
          } yield res

        case Method.GET -> Root / "teams" =>
          for {
            teams <- selectTeams
            res = Response.json(
              """{"teams": [""" + teams
                .map(t => s""""$t"""")
                .mkString(",") + """]})"""
            )
          } yield res

        case Method.GET -> Root / "team" / teamName =>
          getTeamInformation(teamName).map {
            case Some(teamInfo) => Response.text(teamInfo)
            case None =>
              Response.text(s"No information found for team: $teamName")
          }
        /*  case Method.GET -> Root / "games" => ???
        case Method.GET -> Root / "predict" / "game" / gameId => ???*/
      }
      .withDefaultErrorResponse

  val app: ZIO[ZConnectionPool & Server, Throwable, Unit] = for {
    conn <- create *> insertTeams
    _ <- Server.serve(endpoints)
  } yield ()

  override def run: ZIO[Any, Throwable, Unit] =
    app.provide(createZIOPoolConfig >>> connectionPool, Server.default)
}
