package bootstrap.liftweb

import net.liftweb.common.Empty
import net.liftweb.http.{LiftSession, LiftRules}
import net.liftweb.util.Helpers
import travel.offers.backend.DataFetcher


object Scoped {




}


class Boot {





  def boot {
    LiftRules.addToPackages("travel.offers")

    LiftRules.early.append(r => r.setCharacterEncoding("UTF-8"))

    LiftRules.defaultHeaders = {
     case _ =>
       //todo set max age to something sensible
        List("Date" -> Helpers.nowAsInternetDate,
          "Cache-Control" -> "public, max-age=1",
          "Content-Type" -> "text/html; charset=UTF-8")
    }

    LiftRules.autoIncludeAjax = _ => false

    LiftRules.autoIncludeComet = _ => false

    //these are needed to work in Appengine
    LiftRules.enableContainerSessions = false
    LiftRules.getLiftSession = req => new LiftSession(req.contextPath, "dummySession", Empty)
    LiftRules.sessionCreator = (i1, i2) => error("no sessions here please")

    //all paths are stateless (removes lift_page JS on bottom of page
    LiftRules.statelessTest.prepend({case _ => true})

    LiftRules.statelessDispatchTable.append(DataFetcher)
  }


}