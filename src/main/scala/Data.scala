package mlb

import zio.json._
import zio.jdbc._

import java.time.LocalDate
import org.h2.tools.Csv

object HomeTeams {

  opaque type HomeTeam = String

  object HomeTeam {

    def apply(value: String): HomeTeam = value

    def unapply(homeTeam: HomeTeam): String = homeTeam
  }

  given CanEqual[HomeTeam, HomeTeam] = CanEqual.derived
  implicit val homeTeamEncoder: JsonEncoder[HomeTeam] = JsonEncoder.string
  implicit val homeTeamDecoder: JsonDecoder[HomeTeam] = JsonDecoder.string
}

object AwayTeams {

  opaque type AwayTeam = String

  object AwayTeam {

    def apply(value: String): AwayTeam = value

    def unapply(awayTeam: AwayTeam): String = awayTeam
  }

  given CanEqual[AwayTeam, AwayTeam] = CanEqual.derived
  implicit val awayTeamEncoder: JsonEncoder[AwayTeam] = JsonEncoder.string
  implicit val awayTeamDecoder: JsonDecoder[AwayTeam] = JsonDecoder.string
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
import HomeTeams.*
import AwayTeams.*
import Probs.*
import Ratings.*
import Scores.*


final case class Game(
    date: GameDate,
    season: SeasonYear,
    homeTeam: HomeTeam,
    awayTeam: AwayTeam,
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
  ): (GameDate, SeasonYear, HomeTeam, AwayTeam, Rating, Rating, Prob, Prob, Option[Rating], Option[Rating], Rating, Rating, Prob, Prob, Option[Rating], Option[Rating], Option[Score], Option[Score]) =
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
        HomeTeam.unapply(h),
        AwayTeam.unapply(a),
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
        HomeTeam(home),
        AwayTeam(away),
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
  team: HomeTeam,
  elo: Rating,
  season: SeasonYear
)

object Ranking {
  given CanEqual[Ranking, Ranking] = CanEqual.derived
  implicit val codec: JsonCodec[Ranking] = DeriveJsonCodec.gen[Ranking]
  implicit val rankingEncoder: JsonEncoder[Ranking] = DeriveJsonEncoder.gen[Ranking]
  implicit val rankingDecoder: JsonDecoder[Ranking] = DeriveJsonDecoder.gen[Ranking]

  def unapply(ranking: Ranking): (Int, HomeTeam, Rating, SeasonYear) =
    (ranking.position, ranking.team, ranking.elo, ranking.season)

  type Row = (Int, String, Float, Int)

  extension (r: Ranking)
    def toRow: Row =
      val (p, t, e, s) = Ranking.unapply(r)
      (
        p,
        HomeTeam.unapply(t),
        Rating.unapply(e),
        SeasonYear.unapply(s)
      )

  implicit val jdbcDecoder: JdbcDecoder[Ranking] = JdbcDecoder[Row]().map[Ranking] {
    t =>
      val (position, team, elo, season) = t
      Ranking(
        position,
        HomeTeam(team),
        Rating(elo),
        SeasonYear(season)
      )
  }
}
