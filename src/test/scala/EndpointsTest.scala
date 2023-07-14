package mlb

import munit._
import zio._
import zio.http._

class MlbApiSpec extends munit.ZSuite {

  val app: App[Any] = mlb.Endpoints.static

  testZ("should be ok") {
    val req = Request.get(URL(Root / "text"))
    assertZ(app.runZIO(req).isSuccess)
  }

  testZ("should be ko") {
    val req = Request.get(URL(Root))
    assertZ(app.runZIO(req).isFailure)
  }

  testZ("should be initialized") {
    val req = Request.get(URL(Root / "init"))
    assertZ(app.runZIO(req).isSuccess)
    assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 200))
  }

  testZ("should be games count") {
    val req = Request.get(URL(Root / "games" / "count"))
    assertZ(app.runZIO(req).isSuccess)
    assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 200))
  }

  testZ("should be No game in historical data") {
    val req = Request.get(URL(Root / "games" / "count_fail"))
    assertZ(app.runZIO(req).isSuccess)
    assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 204))
  }

  testZ("should be games 1 found") {
      val req = Request.get(URL(Root / "games" / "2020"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 200))
  }

  testZ("should be No games found") {
      val req = Request.get(URL(Root / "no_games" / "no_season"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 204))
  }

  testZ("should be teams 1 win") {
      val req = Request.get(URL(Root / "games" / "win" / "BOS" / "NYY"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 200))
  }

  testZ("should be No match found between these teams") {
      val req = Request.get(URL(Root / "no_games" / "no_win" / "BOS" / "NYY"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 204))
  }

  testZ("should be probability of 1 winning against 2") {
      val req = Request.get(URL(Root / "games" / "probs" / "BOS" / "NYY"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 200))
  }

  testZ("should be No match found between these teams") {
      val req = Request.get(URL(Root / "no_games" / "no_probs" / "BOS" / "NYY"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 204))
  }

  testZ("should be Ranking") {
      val req = Request.get(URL(Root / "ranking" / "2020"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 200))
  }

  testZ("should be There is no data for this season") {
      val req = Request.get(URL(Root / "no_ranking" / "2020"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 204))
  }

  testZ("should be Historic") {
      val req = Request.get(URL(Root / "historic"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 200))
  }

  testZ("should be Something went wrong :/") {
      val req = Request.get(URL(Root / "no_historic"))
      assertZ(app.runZIO(req).isSuccess)
      assertZ(app.runZIO(req).map(_.status).map(_.code).map(_ == 204))
  }
}