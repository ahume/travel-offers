package travel.offers.snippet

import net.liftweb.util._
import Helpers._
import net.liftweb.http.S
import appenginehelpers.{Response, ExpirationSeconds, UrlFetcher, HybridCache}
import travel.offers.backend.{Offer, Keyword}
import xml.{Unparsed, NodeSeq, XML}

class TravelOffers extends UrlFetcher with HybridCache {

  def top = {

    val a = {

    val shortUrl: String = S.param("url").openOr(throw new RuntimeException("No short url specified")).replace("http://www.guardian.co.uk/", "").replace("http://www.gucode.co.uk/", "").replace("http://www.gucode.gnl/", "")

    val apiUrl = "http://content.guardianapis.com/%s?format=xml&show-tags=keyword".format(shortUrl)

    GET(apiUrl, None, ExpirationSeconds(20 * 60)) match {
      case Response(200, Some(xmlString), _) => {

        val keywordsFromPage = (XML.loadString(xmlString) \\ "tags" \\ "@id") map {  _.text } map { Keyword(_) }

        cache.get("offers").map {
          case offers: List[Offer] => {
            val candidateOffers = offers filter { _.keywords.intersect(keywordsFromPage).size > 0 }
            val theOffer: Option[Offer] = candidateOffers.sortBy{ _.keywords.intersect(keywordsFromPage).size}.headOption

            theOffer map { o =>
              (".offer-image [src]" #> o.imageUrl) &
                (".headline *" #> o.title) &
                (".buy-link [href]" #> (o.offerUrl + "?INTCMP=ILCTOFFTXT10390")) &
                (".buy-link *" #> Unparsed("Starting " + o.earliestDeparture.toString("EEEE dd MMMM yyyy") + " from &pound;" + o.fromPrice)) &
                (".header-image [title]" #> o.keywords.intersect(keywordsFromPage).map(_.id).mkString(","))
            } getOrElse ("#travel-offers-top" #> "")
          }
          case a => "#travel-offers-top *" #> ""
        } getOrElse ("#travel-offers-top *" #> "")
      }
      case _ => "#travel-offers-top *" #> ""
    }
  }
    a
  }

}