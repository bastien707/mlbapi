package mlb

import zio._
import zio.jdbc._
import zio.http._
import com.github.tototoshi.csv._
import zio.json.EncoderOps
import mlb.MLBData
import mlb.Team
import mlb.Score

object MlbApi extends ZIOAppDefault {

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

  /***********************************/
  /** Insert data into the database **/
  /***********************************/

  val Teams =
    mlbDataList.foldLeft(Set.empty[String]) { (distinctTeams, row) =>
      distinctTeams + row.team1
    }

  val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"""
        CREATE TABLE IF NOT EXISTS teams (
          id SERIAL PRIMARY KEY,
          name VARCHAR(255),
          elo FLOAT,
          rating FLOAT
        );
        CREATE TABLE GAMES (
          id SERIAL PRIMARY KEY,
          date VARCHAR(255),
          season VARCHAR(255),
          team1 VARCHAR(255),
          team2 VARCHAR(255),
          elo1Pre FLOAT,
          elo2Pre FLOAT,
          eloProb1 FLOAT,
          eloProb2 FLOAT,
          rating1Pre FLOAT,
          rating2Pre FLOAT,
          ratingProb1 FLOAT,
          ratingProb2 FLOAT
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
            INSERT INTO teams (name, elo, rating)
            VALUES ($team, 0, 0)
          """
          )
        }
      )
      .map(_.headOption.getOrElse(throw new Exception("No rows inserted")))
  }

  val insertGames: ZIO[ZConnectionPool, Throwable, UpdateResult] = transaction {
    ZIO
      .collectAll(
        mlbDataList.map { game =>
          insert(
            sql"""
            INSERT INTO games (date, season, team1, team2, elo1Pre, elo2Pre, eloProb1, eloProb2, rating1Pre, rating2Pre, ratingProb1, ratingProb2)
            VALUES (${game.date}, ${game.season}, ${game.team1}, ${game.team2}, ${game.elo1Pre}, ${game.elo2Pre}, ${game.eloProb1}, ${game.eloProb2}, ${game.rating1Pre}, ${game.rating2Pre}, ${game.ratingProb1}, ${game.ratingProb2})
          """
          )
        }
      )
      .map(_.headOption.getOrElse(throw new Exception("No rows inserted")))
  }

  /***********************************/
  /** Select data from the database **/
  /***********************************/

  val countTeams: ZIO[ZConnectionPool, Throwable, Option[String]] =
    transaction {
      selectOne(
        sql"SELECT COUNT(*) FROM teams".as[String]
      )
    }

  val getGames: ZIO[ZConnectionPool, Throwable, zio.Chunk[MLBData]] = transaction {
    selectAll(
      sql"""
        SELECT * FROM games
      """.as[MLBData]
    )
  }

  def getGames(team1: String, team2: String): ZIO[ZConnectionPool, Throwable, zio.Chunk[MLBData]] = transaction {
    selectAll(
      sql"""
        SELECT * FROM games WHERE team1 = $team1 AND team2 = $team2
      """.as[MLBData]
    )
  }

  // if return type is Team this mean we select *
  val selectTeams: ZIO[ZConnectionPool, Throwable, zio.Chunk[Team]] =
    transaction {
      selectAll(
        sql"SELECT * FROM teams".as[Team]
      )
    }
  
  def getRating(teamName: String): ZIO[ZConnectionPool, Throwable, zio.Chunk[Score]] = 
    transaction {
      selectAll(
        sql"SELECT elo, rating FROM teams WHERE name = $teamName".as[Score]
      )
    }

  def getTeamInformation(
      teamName: String
  ): ZIO[ZConnectionPool, Throwable, Option[Team]] = transaction {
    selectOne(
      sql"""
        SELECT * FROM teams WHERE name = $teamName
      """.as[Team]
    )
  }

  /***************************************************/
  /** Configuration for the ZIO JDBC connection pool */
  /***************************************************/

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

  /*******************************************/
  /** Endpoints for the ZIO HTTP application */
  /*******************************************/

  val endpoints: App[ZConnectionPool] =
    Http
      .collectZIO[Request] {

        case Method.GET -> Root / "team" / "count" =>
          for {
            count: Option[String] <- countTeams
            res: Response = count match
              case Some(c) => Response.text(s"${c} teams")
              case None => Response.text("No team in historical data")
          } yield res

        case Method.GET -> Root / "team" / "all" =>
          for {
            teams <- selectTeams
            teamsJson = teams.toJson
            res = Response.json(
              s"""{"teams": ${teamsJson.toString}}"""
            )
          } yield res

        case Method.GET -> Root / "team" / teamName =>
          getTeamInformation(teamName).map {
            case Some(team) => Response.json(s"""{"team": ${team.toJson.toString}}""")
            case None => Response.text(s"No information found for team: $teamName")
          }

        case Method.GET -> Root / "score" / teamName =>
          getRating(teamName).map {
            case Chunk(score) => Response.json(s"""{"${teamName}": ${score.toJson.toString}}""")
            case _ => Response.text(s"No information found for team: $teamName")
          }

        case Method.GET -> Root / "games" / "all" =>
          for {
            games <- getGames
            gamesJson = games.toJson
            res = Response.json(
              s"""{"games": ${gamesJson.toString}}"""
            )
          } yield res

        case Method.GET -> Root / "games" / team1 / team2 =>
          for {
            games <- getGames(team1, team2)
            gamesJson = games.toJson
            res = Response.json(
              s"""{"games": ${gamesJson.toString}}"""
            )
          } yield res
        /*  case Method.GET -> Root / "games" => ???
        case Method.GET -> Root / "predict" / "game" / gameId => ???*/
      }
      .withDefaultErrorResponse

  val app: ZIO[ZConnectionPool & Server, Throwable, Unit] = for {
    conn <- create *> insertTeams *> insertGames
    _ <- Server.serve(endpoints)
  } yield ()

  override def run: ZIO[Any, Throwable, Unit] =
    app.provide(createZIOPoolConfig >>> connectionPool, Server.default)
}
