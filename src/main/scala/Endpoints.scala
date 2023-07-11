package mlb

import zio._
import zio.jdbc._
import zio.http._
import zio.json.EncoderOps
import Database.*
import mlb.HomeTeams.HomeTeam
import mlb.AwayTeams.AwayTeam
import mlb.SeasonYears.SeasonYear

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
            ZIO.succeed(Response.text("Database initialization failed").withStatus(Status.InternalServerError))
          }


        case Method.GET -> Root / "games" / "count" =>
          (for {
            count: Option[String] <- countGames
            res: Response = count match
              case Some(c) => Response.text(s"${c} games")
              case None    => Response.text("No game in historical data").withStatus(Status.NoContent)
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("No games found").withStatus(Status.NotFound))
          }

        
        case Method.GET -> Root / "games" / season => 
          (for {
            games <- selectGames(SeasonYear(season.toInt))
            res = games match {
              case Some(value) => 
                val json = value.toJson
                Response.json("""{"games": """ + json + """}""")
              case None => Response.text("No games found").withStatus(Status.NoContent)
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("No games found").withStatus(Status.NotFound))
          }


        case Method.GET -> Root / "games" / "win" / team1 / team2 =>
          (for {
            games <- getVictoriesNumber(HomeTeam(team1), AwayTeam(team2))
            res = games match {
              case Some(value) => 
                val json = value.toJson
                Response.json("""{"wins": """ + json + """}""")
              case None => Response.text("No games found").withStatus(Status.NoContent)
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("No games found").withStatus(Status.NotFound))
          }
      }
      .withDefaultErrorResponse
}
