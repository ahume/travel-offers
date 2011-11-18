package travel.offers

import backend.Offer._
import backend.{Appengine, Keyword, Offer}
import org.joda.time.DateTime
import net.liftweb.http.{S, RequestVar}
import java.lang.IllegalStateException
import xml.XML
import appenginehelpers.{Response, ExpirationSeconds}
import scala.util.Random

object Scoped {

  val apiKey = "w6uczudeaz692ej822b5sffj"

  val campaigns = Map(
    "promo-1-offer" -> "11111111111111111111",
    "promo-2-offer" -> "22222222222222222222",
    "narrow-grey" -> "33333333333333333333",
    "narrow-blue" -> "33333333333333333333"
  )

  object backgroundColor extends RequestVar[String](throw new IllegalStateException("someone should have set the background color"))

  object campaign extends RequestVar[String](throw new IllegalStateException("someone should have set the campaign"))

  object numTrails extends RequestVar[Int](throw new IllegalStateException("someone should have set the number of offers"))

  object imageSize extends RequestVar[String](throw new IllegalStateException("someone should have set the image size"))

  object offers extends RequestVar[List[Offer]]( getOffersFor (pageUrl) take numTrails.get )

  private def defaultOffers = Random.shuffle(
    Scoped.imageSize.get match {
      case "ThumbOne" => List(
        defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/18/1321614311421/Holiday-Offers---train-tr-001.jpg"),
        defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/18/1321614346487/Holiday-Offers-New-York-001.jpg"),
        defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/18/1321614378671/Holiday-Offers-India-001.jpg"),
        defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/18/1321614408885/Holiday-Offers-Paris-001.jpg")
      )
      case "TwoColumn" => List(
        defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/16/1321460655227/Holiday-offers-image---tr-003.jpg"),
        defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/16/1321460714281/Holiday-Offers-image---Ne-003.jpg"),
        defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/16/1321460772820/Holiday-offers-image---In-003.jpg"),
        defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/16/1321460806064/Holiday-Offers-image---Pa-003.jpg")

      )
    }
  )

  private def defaultOffer(image: String) = Offer(-1, None, "http://www.guardianholidayoffers.co.uk/", image,
      "", new DateTime, Nil, Nil)

  private def getOffersFor(pageUrl: String): List[Offer] = getRealOffersFor(pageUrl) ++ defaultOffers

  private def getRealOffersFor(pageUrl: String) =  {
    val apiUrl = "http://content.guardianapis.com/%s?format=xml&show-tags=keyword&api-key=%s".format(pageUrl, apiKey)
    Appengine.GET(apiUrl) map { xmlString =>
          val keywordsFromPage = (XML.loadString(xmlString) \\ "tag") map { Keyword(_) }

          Appengine.getOffers.filter { _.keywords.intersect(keywordsFromPage).size > 0 } sortBy { _.keywords.intersect(keywordsFromPage).size } reverse

    } getOrElse Nil
  }

  //yeah, yeah I know, but it works
  private def pageUrl: String = S.param("url").openOr(throw new IllegalStateException("all requests need the parameter 'url'"))
    .replace("http://www.guardian.co.uk/", "")
    .replace("http://www.guprod.gnl/", "")
    .replace("http://www.gucode.co.uk/", "")
    .replace("http://www.gucode.gnl/", "")
    .replace("http://www.guqa.co.uk/", "")
    .replace("http://www.guqa.gnl/", "")
    .replace("http://www.gurelease.co.uk/", "")
    .replace("http://www.guqa.gnl/", "")
}