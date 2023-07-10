// Database.scala
package mlb

import zio._
import zio.jdbc._

object Database {

  val gamesCsv = CsvParser.parseGame()
  print(gamesCsv)
  print("#############", games)

  val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"""
      CREATE TABLE IF NOT EXISTS games(
        id SERIAL PRIMARY KEY,
        date VARCHAR(255),
        season_year INT,
        playoff_round INT,
        home_team VARCHAR(255),
        away_team VARCHAR(255)
      );
    """
    )
  }
  val initializeDatabaseLogic: ZIO[ZConnectionPool, Throwable, Unit] =
    for {
      _ <- create
      _ <- insertRows
    } yield ()

  val dropTables: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(sql"""DROP TABLE IF EXISTS games;""")
  }

  val insertRows: ZIO[ZConnectionPool, Throwable, UpdateResult] = {
    val rows: List[Game.Row] = gamesCsv.map(_.toRow)
    transaction {
      insert(
        sql"INSERT INTO games(date, season_year, playoff_round, home_team, away_team)"
          .values[Game.Row](rows)
      )
    }
  }

  val countGames: ZIO[ZConnectionPool, Throwable, Option[String]] =
    transaction {
      selectOne(
        sql"SELECT COUNT(*) FROM games".as[String]
      )
    }

  val selectGames: ZIO[ZConnectionPool, Throwable, Option[Chunk[Game]]] =
    transaction {
      selectAll(
        sql"SELECT date, season_year, playoff_round, home_team, away_team FROM games LIMIT 5"
          .as[Game]
      ).map(Option(_))
    }
}
