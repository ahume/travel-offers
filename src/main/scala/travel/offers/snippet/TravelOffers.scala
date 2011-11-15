package travel.offers.snippet

import net.liftweb.util._
import Helpers._
import appenginehelpers.{Response, ExpirationSeconds, UrlFetcher, HybridCache}
import travel.offers.backend.{Offer, Keyword}
import xml.{Unparsed, NodeSeq, XML}
import java.lang.IllegalStateException
import collection.immutable.List
import net.liftweb.http.{RequestVar, S}

class TravelOffers {

  def search = {
    val offer = Repository.offers.get(0)
    "#travel-offer [value]" #> offer.keywords(0).name
  }

  def trail = {
    val offer = Repository.offers.get(0)
    (".offer-image [src]" #> offer.imageUrl) &
    (".link [href]" #> offer.offerUrl) &
    (".offer-image [alt]" #> offer.title) &
    (".lift-offer *" #> offer.title ) &
    (".lift-offer [class]" #> "link")
  }
}



object Repository extends UrlFetcher with HybridCache {

  object offers extends RequestVar[List[Offer]](getOffers)



  private def getOffers = {
    val url: String = S.param("url").openOr(throw new RuntimeException("No short url specified")).replace("http://www.guardian.co.uk/", "").replace("http://www.gucode.co.uk/", "").replace("http://www.gucode.gnl/", "")
    val apiUrl = "http://content.guardianapis.com/%s?format=xml&show-tags=keyword".format(url)

    val offerOption: Option[List[Offer]] = GET(apiUrl, None, ExpirationSeconds(20 * 60)) match {
      case Response(200, Some(xmlString), _) => {

        val keywordsFromPage = (XML.loadString(xmlString) \\ "tags") map { Keyword(_) }

        println(keywordsFromPage)

        cache.get("offers").map {
          case offers: List[Offer] => offers filter {
            println(offers)
            _.keywords.intersect(keywordsFromPage).size > 0
          }
          case a => throw new IllegalStateException("should not have got a " + a.getClass)
        }
      }
      case _ => throw new IllegalStateException("oops")
    }
    offerOption.getOrElse (Nil)
  }

}