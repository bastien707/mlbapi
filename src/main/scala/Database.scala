// Database.scala
package mlb

import zio._
import zio.jdbc._

object Database {

  val mlbDataList = CsvParser.parseMlbData()

  val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"""
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
          elo1Post FLOAT,
          elo2Post FLOAT,
          rating1Pre FLOAT,
          rating2Pre FLOAT,
          pitcher1 VARCHAR(255),
          pitcher2 VARCHAR(255),
          pitcher1Rgs FLOAT,
          pitcher2Rgs FLOAT,
          pitcher1Adj FLOAT,
          pitcher2Adj FLOAT,
          ratingProb1 FLOAT,
          ratingProb2 FLOAT,
          rating1Post FLOAT,
          rating2Post FLOAT,
          score1 INT,
          score2 INT
        );
      """
    )
  }

  val insertGames: ZIO[ZConnectionPool, Throwable, UpdateResult] = transaction {
    ZIO
      .collectAll(
        mlbDataList.map { game =>
          insert(
            sql"""
            INSERT INTO games (date, season, team1, team2, elo1Pre, elo2Pre, eloProb1, eloProb2, elo1post, elo2post, rating1Pre, rating2Pre, pitcher1, pitcher2, pitcher1Rgs, pitcher2Rgs, pitcher1Adj, pitcher2Adj, ratingProb1, ratingProb2, rating1Post, rating2Post, score1, score2)
            VALUES (${game.date}, ${game.season}, ${game.team1}, ${game.team2}, ${game.elo1Pre}, ${game.elo2Pre} ,${game.eloProb1}, ${game.eloProb2}, ${game.elo1post}, ${game.elo2post}, ${game.rating1Pre}, ${game.rating2Pre}, ${game.pitcher1}, ${game.pitcher2}, ${game.pitcher1rgs}, ${game.pitcher2rgs}, ${game.pitcher1adj}, ${game.pitcher2adj}, ${game.ratingProb1}, ${game.ratingProb2}, ${game.rating1Post}, ${game.rating2Post}, ${game.score1}, ${game.score2})
          """
          )
        }
      )
      .map(_.headOption.getOrElse(throw new Exception("No rows inserted")))
  }

  val getGames: ZIO[ZConnectionPool, Throwable, Option[Chunk[MLBData]]] =
    transaction {
      selectAll(
        sql"SELECT * FROM games LIMIT 5;".as[MLBData]
      ).map(Option(_))
    }

  val countGames: ZIO[ZConnectionPool, Throwable, Option[String]] =
    transaction {
      selectOne(
        sql"SELECT COUNT(*) FROM games".as[String]
      )
    }

  def getGames(
      team1: String,
      team2: String
  ): ZIO[ZConnectionPool, Throwable, Option[Chunk[MLBData]]] = transaction {
    selectAll(
      sql"""
        SELECT * FROM games WHERE team1 = $team1 AND team2 = $team2
      """.as[MLBData]
    ).map(Option(_))
  }
}
