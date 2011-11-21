package travel.offers.backend

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.S
import travel.offers.Appengine._
import travel.offers.Scoped._

object OffersList extends RestHelper {
  serve {
    case Get("offers" ::  Nil, _) =>
      S.setHeader("Content-Type", "text/html;charset=utf-8")
      S.setHeader("Cache-Control", "public, max-age=1")
      <html>
        <body>
          <h3>Campaign codes</h3>
          <ul>{
            campaigns map { case (key, value) => <li>{key} -> {value}</li> }
          }</ul>
          <h3>Offers</h3>
          <ul>{
            getOffers map { offer =>
              <li>
                <b>{ offer.title.getOrElse("") }</b> : {offer.keywords.map(_.id).mkString(", ")}
              </li>
            }
          }</ul>
        </body>
      </html>
  }
}