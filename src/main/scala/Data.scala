package mlb

import zio.json._
import zio.jdbc._

import java.time.LocalDate
import org.h2.tools.Csv

object Teams {
  
    opaque type Team = String
  
    object Team {
  
      def apply(value: String): Team = value
  
      def unapply(team: Team): String = team
    }
  
    given CanEqual[Team, Team] = CanEqual.derived
    implicit val teamEncoder: JsonEncoder[Team] = JsonEncoder.string
    implicit val teamDecoder: JsonDecoder[Team] = JsonDecoder.string
}

object GameDates {

  opaque type GameDate = LocalDate

  object GameDate {

    def apply(value: LocalDate): GameDate = value

    def unapply(gameDate: GameDate): LocalDate = gameDate
  }

  given CanEqual[GameDate, GameDate] = CanEqual.derived
  implicit val gameDateEncoder: JsonEncoder[GameDate] = JsonEncoder.localDate
  implicit val gameDateDecoder: JsonDecoder[GameDate] = JsonDecoder.localDate
}

object SeasonYears {

  opaque type SeasonYear <: Int = Int

  object SeasonYear {

    def apply(year: Int): SeasonYear = year

    def safe(value: Int): Option[SeasonYear] =
      Option.when(value >= 1876 && value <= LocalDate.now.getYear)(value)

    def unapply(seasonYear: SeasonYear): Int = seasonYear
  }

  given CanEqual[SeasonYear, SeasonYear] = CanEqual.derived
  implicit val seasonYearEncoder: JsonEncoder[SeasonYear] = JsonEncoder.int
  implicit val seasonYearDecoder: JsonDecoder[SeasonYear] = JsonDecoder.int
}

// For elos and ratings probabilities
object Probs {

  opaque type Prob = Float

  object Prob {

    def apply(value: Float): Prob = value

    def safe(value: Float): Option[Prob] =
      Option.when(value >= 0 && value <= 1)(value)

    def unapply(prob: Prob): Float = prob
  }

  given CanEqual[Prob, Prob] = CanEqual.derived
  implicit val probEncoder: JsonEncoder[Prob] = JsonEncoder.float
  implicit val probDecoder: JsonDecoder[Prob] = JsonDecoder.float
}

// For elos and ratings
object Ratings {

  opaque type Rating = Float

  object Rating {

    def apply(value: Float): Rating = value

    def safe(value: Float): Option[Rating] =
      Option.when(value >= 0)(value)

    def unapply(rating: Rating): Float = rating
  }

  given CanEqual[Rating, Rating] = CanEqual.derived
  implicit val ratingEncoder: JsonEncoder[Rating] = JsonEncoder.float
  implicit val ratingDecoder: JsonDecoder[Rating] = JsonDecoder.float
}

object Scores {

  opaque type Score = Int

  object Score {

    def apply(value: Int): Score = value

    def safe(value: Int): Option[Score] =
      Option.when(value >= 0)(value)

    def unapply(score: Score): Int = score
  }

  given CanEqual[Score, Score] = CanEqual.derived
  implicit val scoreEncoder: JsonEncoder[Score] = JsonEncoder.int
  implicit val scoreDecoder: JsonDecoder[Score] = JsonDecoder.int

}

import GameDates.*
import SeasonYears.*
import Probs.*
import Ratings.*
import Scores.*
import Teams.*

final case class Game(
    date: GameDate,
    season: SeasonYear,
    homeTeam: Team,
    awayTeam: Team,
    elo1_pre: Rating, 
    elo2_pre: Rating, 
    elo_prob1: Prob,
    elo_prob2: Prob, 
    elo1_post: Option[Rating],
    elo2_post: Option[Rating],
    rating1_pre: Rating,
    rating2_pre: Rating,
    rating_prob1: Prob,
    rating_prob2: Prob,
    rating1_post: Option[Rating],
    rating2_post: Option[Rating],
    score1: Option[Score],
    score2: Option[Score]
)

object Game {

  given CanEqual[Game, Game] = CanEqual.derived
  implicit val codec: JsonCodec[Game] = DeriveJsonCodec.gen[Game]
  implicit val gameEncoder: JsonEncoder[Game] = DeriveJsonEncoder.gen[Game]
  implicit val gameDecoder: JsonDecoder[Game] = DeriveJsonDecoder.gen[Game]

  def unapply(
      game: Game
  ): (GameDate, SeasonYear, Team, Team, Rating, Rating, Prob, Prob, Option[Rating], Option[Rating], Rating, Rating, Prob, Prob, Option[Rating], Option[Rating], Option[Score], Option[Score]) =
    (
      game.date,
      game.season,
      game.homeTeam,
      game.awayTeam,
      game.elo1_pre,
      game.elo2_pre,
      game.elo_prob1,
      game.elo_prob2,
      game.elo1_post,
      game.elo2_post,
      game.rating1_pre,
      game.rating2_pre,
      game.rating_prob1,
      game.rating_prob2,
      game.rating1_post,
      game.rating2_post,
      game.score1,
      game.score2
    )

  // a custom decoder from a tuple
  type Row = (String, Int, String, String, Float, Float, Float, Float, Option[Float], Option[Float], Float, Float, Float, Float, Option[Float], Option[Float], Option[Int], Option[Int])

  extension (g: Game)
    def toRow: Row =
      val (d, y, h, a, e1p, e2p, ep1, ep2, e1post, e2post, r1p, r2p, rp1, rp2, r1post, r2post, s1, s2) = Game.unapply(g)
      (
        GameDate.unapply(d).toString,
        SeasonYear.unapply(y),
        Team.unapply(h),
        Team.unapply(a),
        Rating.unapply(e1p),
        Rating.unapply(e2p),
        Prob.unapply(ep1),
        Prob.unapply(ep2),
        e1post.map(Rating.unapply),
        e2post.map(Rating.unapply),
        Rating.unapply(r1p),
        Rating.unapply(r2p),
        Prob.unapply(rp1),
        Prob.unapply(rp2),
        r1post.map(Rating.unapply),
        r2post.map(Rating.unapply),
        s1.map(Score.unapply),
        s2.map(Score.unapply)
      )

  implicit val jdbcDecoder: JdbcDecoder[Game] = JdbcDecoder[Row]().map[Game] {
    t =>
      val (date, season, home, away, elo1_pre, elo2_pre, elo_prob1, elo_prob2, elo1_post, elo2_post, rating1_pre, rating2_pre, rating_prob1, rating_prob2, rating1_post, rating2_post, score1, score2) = t
      Game(
        GameDate(LocalDate.parse(date)),
        SeasonYear(season),
        Team(home),
        Team(away),
        Rating(elo1_pre),
        Rating(elo2_pre),
        Prob(elo_prob1),
        Prob(elo_prob2),
        elo1_post.map(Rating(_)),
        elo2_post.map(Rating(_)),
        Rating(rating1_pre),
        Rating(rating2_pre),
        Prob(rating_prob1),
        Prob(rating_prob2),
        rating1_post.map(Rating(_)),
        rating2_post.map(Rating(_)),
        score1.map(Score(_)),
        score2.map(Score(_))
      )
  }
}

final case class Wins (
  year: SeasonYear,
  wins: Score
)

object Wins {
  given CanEqual[Wins, Wins] = CanEqual.derived
  implicit val codec: JsonCodec[Wins] = DeriveJsonCodec.gen[Wins]
  implicit val winsEncoder: JsonEncoder[Wins] = DeriveJsonEncoder.gen[Wins]
  implicit val winsDecoder: JsonDecoder[Wins] = DeriveJsonDecoder.gen[Wins]

  def unapply(wins: Wins): (SeasonYear, Score) =
    (wins.year, wins.wins)

  type Row = (Int, Int)

  extension (w: Wins)
    def toRow: Row =
      val (y, wi) = Wins.unapply(w)
      (
        SeasonYear.unapply(y),
        Score.unapply(wi)
      )

  implicit val jdbcDecoder: JdbcDecoder[Wins] = JdbcDecoder[Row]().map[Wins] {
    t =>
      val (year, wins) = t
      Wins(
        SeasonYear(year),
        Score(wins)
      )
  }
}

final case class Ranking (
  position: Int,
  team: Team,
  elo: Rating,
  season: SeasonYear
)

object Ranking {
  given CanEqual[Ranking, Ranking] = CanEqual.derived
  implicit val codec: JsonCodec[Ranking] = DeriveJsonCodec.gen[Ranking]
  implicit val rankingEncoder: JsonEncoder[Ranking] = DeriveJsonEncoder.gen[Ranking]
  implicit val rankingDecoder: JsonDecoder[Ranking] = DeriveJsonDecoder.gen[Ranking]

  def unapply(ranking: Ranking): (Int, Team, Rating, SeasonYear) =
    (ranking.position, ranking.team, ranking.elo, ranking.season)

  type Row = (Int, String, Float, Int)

  extension (r: Ranking)
    def toRow: Row =
      val (p, t, e, s) = Ranking.unapply(r)
      (
        p,
        Team.unapply(t),
        Rating.unapply(e),
        SeasonYear.unapply(s)
      )

  implicit val jdbcDecoder: JdbcDecoder[Ranking] = JdbcDecoder[Row]().map[Ranking] {
    t =>
      val (position, team, elo, season) = t
      Ranking(
        position,
        Team(team),
        Rating(elo),
        SeasonYear(season)
      )
  }
}

final case class Historics (
  season: SeasonYear,
  team: Team,
  victories: Score,
  defeats: Score,
  draws: Score,
)

object Historics {
  given CanEqual[Historics, Historics] = CanEqual.derived
  implicit val codec: JsonCodec[Historics] = DeriveJsonCodec.gen[Historics]
  implicit val historicsEncoder: JsonEncoder[Historics] = DeriveJsonEncoder.gen[Historics]
  implicit val historicsDecoder: JsonDecoder[Historics] = DeriveJsonDecoder.gen[Historics]

  def unapply(historics: Historics): (SeasonYear, Team, Score, Score, Score) =
    (historics.season, historics.team, historics.victories, historics.defeats, historics.draws)

  type Row = (Int, String, Int, Int, Int)

  extension (h: Historics)
    def toRow: Row =
      val (s, t, v, d, dr) = Historics.unapply(h)
      (
        SeasonYear.unapply(s),
        Team.unapply(t),
        Score.unapply(v),
        Score.unapply(d),
        Score.unapply(dr)
      )

  implicit val jdbcDecoder: JdbcDecoder[Historics] = JdbcDecoder[Row]().map[Historics] {
    t =>
      val (season, team, victories, defeats, draws) = t
      Historics(
        SeasonYear(season),
        Team(team),
        Score(victories),
        Score(defeats),
        Score(draws)
      )
  }
}