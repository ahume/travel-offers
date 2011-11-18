package travel.offers.backend

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.S

object OffersList extends RestHelper {
  serve {
    case Get("offers" ::  Nil, _) =>
      S.setHeader("Content-Type", "text/html;charset=utf-8")
      <html>
        <body>
          <ul>{
            Appengine.getOffers.map{ offer =>
              <li>
                <b>{ offer.title.getOrElse("") }</b> : {offer.keywords.map(_.id).mkString(", ")}
              </li>
            }
          }</ul>
        </body>
      </html>
  }
}