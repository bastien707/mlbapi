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
        date VARCHAR(255),
        season INT,
        home_team VARCHAR(255),
        away_team VARCHAR(255),
        elo1_pre FLOAT,
        elo2_pre FLOAT,
        elo_prob1 FLOAT,
        elo_prob2 FLOAT,
        elo1_post FLOAT,
        elo2_post FLOAT,
        rating1_pre FLOAT,
        rating2_pre FLOAT,
        rating_prob1 FLOAT,
        rating_prob2 FLOAT,
        rating1_post FLOAT,
        rating2_post FLOAT,
        score1 INT,
        score2 INT
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
        sql"INSERT INTO games(date, season, home_team, away_team, elo1_pre, elo2_pre, elo_prob1, elo_prob2, elo1_post, elo2_post, rating1_pre, rating2_pre, rating_prob1, rating_prob2, rating1_post, rating2_post, score1, score2)"
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

  def selectGames(
      season: SeasonYear
  ): ZIO[ZConnectionPool, Throwable, Option[Chunk[Game]]] =
    transaction {
      selectAll(
        sql"SELECT * FROM games WHERE season = ${SeasonYear.unapply(season)}"
          .as[Game]
      ).map(Option(_))
    }

  def getVictoriesNumber(
      team1: HomeTeam,
      team2: AwayTeam
  ): ZIO[ZConnectionPool, Throwable, Option[Chunk[Wins]]] = transaction {
    selectAll(
      sql"""
      SELECT season, COUNT(*)
      FROM games
      WHERE
        (home_team = ${HomeTeam.unapply(team1)} AND away_team = ${AwayTeam
          .unapply(team2)} AND score1 > score2) OR
        (home_team = ${AwayTeam.unapply(team2)} AND away_team = ${HomeTeam
          .unapply(team1)} AND score2 > score1)
      GROUP BY season
    """.as[(Wins)]
    ).map(Option(_))
  }

  def getLastMatch(
      team1: HomeTeam,
      team2: AwayTeam,
  ): ZIO[ZConnectionPool, Throwable, Option[Game]] = transaction {
    selectOne(
      sql"SELECT * FROM games WHERE (home_team = ${HomeTeam
          .unapply(team1)} AND away_team = ${AwayTeam.unapply(team2)}) OR (home_team = ${AwayTeam
          .unapply(team2)} AND away_team = ${HomeTeam.unapply(team1)}) ORDER BY date DESC LIMIT 1;"
        .as[Game]
    )
  }

  def getTopTen(
    season:SeasonYear
  ): ZIO[ZConnectionPool, Throwable, Option[Chunk[(Int, String, Float, Int)]]] = transaction {
    selectAll(
      sql"""SELECT position, team_name, max_elo, season
            FROM (
              SELECT season, team_name, max_elo, 
                ROW_NUMBER() OVER (PARTITION BY season ORDER BY max_elo DESC) AS position
              FROM (
                SELECT season, home_team AS team_name, MAX(elo1_post) AS max_elo
                FROM games
                WHERE season = ${SeasonYear.unapply(season)}
                GROUP BY season, home_team
                UNION ALL
                SELECT season, away_team AS team_name, MAX(elo2_post) AS max_elo
                FROM games
                WHERE season = ${SeasonYear.unapply(season)}
                GROUP BY season, away_team
              ) AS top_teams
            ) AS ranked_teams
            WHERE position <= 10
            ORDER BY season, position;
      """.as[(Int, String, Float, Int)]
    ).map(Option(_))
  }
}
