// Database.scala
package mlb

import zio._
import zio.jdbc._
import zio.http._
import mlb.Endpoints.endpoints
import mlb.Database.*
import Constants.*
import com.github.tototoshi.csv._
import zio.stream.ZStream
import mlb.GameDates.GameDate
import java.time.LocalDate
import mlb.SeasonYears.SeasonYear
import mlb.HomeTeams.HomeTeam
import mlb.AwayTeams.AwayTeam

object Database {

  val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"""
      CREATE TABLE IF NOT EXISTS games(
        id SERIAL PRIMARY KEY,
        date VARCHAR(255),
        season_year INT,
        home_team VARCHAR(255),
        away_team VARCHAR(255),
        elo1_pre FLOAT,
        elo2_pre FLOAT,
        elo_prob1 FLOAT,
        elo_prob2 FLOAT
      );
    """
    )
  }
  val initializeDatabaseLogic: ZIO[ZConnectionPool, Throwable, Unit] = for {
    _ <- create
    source: CSVReader <- ZIO.succeed(
      CSVReader.open(new java.io.File(Elop1))
    )
    stream: Unit <- ZStream
      .fromIterator[Map[String, String]](source.iteratorWithHeaders)
      .map[Game](CsvParser.parseGame)
      .grouped(1000)
      .foreach(chunk => insertRows(chunk.toList))
    _ <- ZIO.succeed(source.close())
  } yield ()

  val dropTables: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(sql"""DROP TABLE IF EXISTS games;""")
  }

  def insertRows(
      games: List[Game]
  ): ZIO[ZConnectionPool, Throwable, UpdateResult] = {
    val rows: List[Game.Row] = games.map(_.toRow)
    transaction {
      insert(
        sql"INSERT INTO games(date, season_year, home_team, away_team, elo1_pre, elo2_pre, elo_prob1, elo_prob2)"
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
        sql"SELECT date, season_year, home_team, away_team, elo1_pre, elo2_pre, elo_prob1, elo_prob2 FROM games LIMIT 5;"
          .as[Game]
      ).map(Option(_))
    }
}
