package mlb

import zio._
import zio.jdbc._
import zio.http._
import zio.json.EncoderOps
import Database.*
import Teams.Team
import mlb.SeasonYears.SeasonYear
import scala.math.pow
import mlb.Ratings.Rating

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
            ZIO.succeed(Response.text("Something went wrong :/").withStatus(Status.NotFound))
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
            ZIO.succeed(Response.text("Something went wrong :/").withStatus(Status.NotFound))
          }


        case Method.GET -> Root / "games" / "win" / team1 / team2 =>
          (for {
            games <- getVictoriesNumber(Team(team1), Team(team2))
            res = games match {
              case Some(value) => 
                val json = value.toJson
                Response.json("""{"wins": """ + json + """}""")
              case None => Response.text("No match found between these teams").withStatus(Status.NoContent)
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("Something went wrong :/").withStatus(Status.NotFound))
          }

        case Method.GET -> Root / "games" / "probs" / team1 / team2  =>
          (for {
            games <- getLastMatch(Team(team1), Team(team2))
            res = games match {
              case Some(value) => 
                val homeTeamElo = value.elo1_pre
                val awayTeamElo = value.elo2_pre
                val homeTeamWinProbability = 1 / (1 + pow(10, (Rating.unapply(homeTeamElo) - Rating.unapply(awayTeamElo)) / 400.0))
                Response.text(s"Probability of ${team1} winning against ${team2} is ${homeTeamWinProbability}")
              case None => Response.text("No match found between these teams").withStatus(Status.NoContent)
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("Something went wrong :/").withStatus(Status.NotFound))
        }

        case Method.GET -> Root / "ranking" / season  =>
          (for {
            games <- getTopTen(SeasonYear(season.toInt))
            res = games match {
              case Some(value) => 
                val json = value.toJson
                Response.json("""{"Ranking": """ + json + """}""")
              case None => Response.text("There is no data for this season").withStatus(Status.NoContent)
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("Something went wrong :/").withStatus(Status.NotFound))
        }

        case Method.GET -> Root / "historic"  =>
          (for {
            games <- getMatchHistoric()
            res = games match {
              case Some(value) => 
                val json = value.toJson
                Response.json("""{"Historic": """ + json + """}""")
              case None => Response.text("Something went wrong :/").withStatus(Status.NoContent)
            }
          } yield res) catchAll { _ =>
            ZIO.succeed(Response.text("Something went wrong :/").withStatus(Status.NotFound))
        }
      }
      .withDefaultErrorResponse
}
