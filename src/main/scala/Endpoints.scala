package mlb

import zio._
import zio.jdbc._
import zio.http._
import zio.json.EncoderOps
import Database.*

object Endpoints {
  val endpoints: App[ZConnectionPool] =
    Http
      .collectZIO[Request] {

        case Method.GET -> Root / "init" =>
          (for {
            _ <- dropTables
            _ <- initializeDatabaseLogic
            res = Response.text("Database initialized successfully")
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("Database initialization failed"))
          }

        case Method.GET -> Root / "games" / "count" =>
          (for {
            count: Option[String] <- countGames
            res: Response = count match
              case Some(c) => Response.text(s"${c} games")
              case None    => Response.text("No game in historical data")
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("No games found"))
          }

        case Method.GET -> Root / "games" / "latest" =>
          (for {
            gamesOption: Option[Chunk[MLBData]] <- getGames
            res: Response = gamesOption match {
              case Some(games) =>
                val gamesJson = games.toJson
                Response.json(s"""{"games": ${gamesJson}}""")
              case None =>
                Response.text("No games found")
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("No games found"))
          }

        case Method.GET -> Root / "games" / team1 / team2 =>
          (for {
            gamesOption: Option[Chunk[MLBData]] <- getGames(team1, team2)
            res: Response = gamesOption match {
              case Some(games) =>
                val gamesJson = games.toJson
                Response.json(s"""{"games": ${gamesJson}}""")
              case None =>
                Response.text("No games found")
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("No games found"))
          }

        case Method.GET -> Root / "games" / team1 =>
          (for {
            teamsOption: Option[Chunk[Team]] <- selectHomeTeamGames(team1)
            res: Response = teamsOption match {
              case Some(teams) =>
                val teamsJson = teams.toJson
                Response.json(s"""{"teams": ${teamsJson}}""")
              case None =>
                Response.text("No games found")
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("No games found"))
          }

        case Method.GET -> Root / "season" / "averages" / team =>
          (for {
            seasonAverages <- getSeasonAverages(team)
            res = seasonAverages match {
              case averages if averages.nonEmpty =>
                val averagesJson = averages.toJson
                Response.json(s"""{"averages": ${averagesJson}}""")
              case _ =>
                Response.text("No season averages found")
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("No season averages found"))
          }
      }
      .withDefaultErrorResponse
}
