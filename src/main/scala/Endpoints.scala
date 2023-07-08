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

        case Method.GET -> Root / "games" / "count" =>
          for {
            count: Option[String] <- countGames
            res: Response = count match
              case Some(c) => Response.text(s"${c} games")
              case None    => Response.text("No game in historical data")
          } yield res

        case Method.GET -> Root / "games" / "latest" =>
          for {
            gamesOption: Option[Chunk[MLBData]] <- getGames
            res: Response = gamesOption match {
              case Some(games) =>
                val gamesJson = games.toJson
                Response.json(s"""{"games": ${gamesJson}}""")
              case None =>
                Response.text("No games found")
            }
          } yield res

        case Method.GET -> Root / "games" / team1 / team2 =>
          for {
            gamesOption: Option[Chunk[MLBData]] <- getGames(team1, team2)
            res: Response = gamesOption match {
              case Some(games) =>
                val gamesJson = games.toJson
                Response.json(s"""{"games": ${gamesJson}}""")
              case None =>
                Response.text("No games found")
            }
          } yield res
      }
      .withDefaultErrorResponse
}