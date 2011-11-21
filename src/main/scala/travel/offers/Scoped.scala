package travel.offers
import org.joda.time.DateTime
import net.liftweb.http.{S, RequestVar}
import java.lang.IllegalStateException
import xml.XML
import scala.util.Random

object Scoped {

  val apiKey = "w6uczudeaz692ej822b5sffj"

  val campaigns = Map(
    "promo-1-offer" -> "ILCTOFFTXT5262I",
    "promo-2-offer" -> "ILCTOFFTXT5263I",
    "narrow-grey" -> "ILCTOFFTXT5264I",
    "narrow-blue" -> "ILCTOFFTXT5265I",
    "default" -> "ILCTOFFTXT5266I"
  )

  val trainsSmall = defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/18/1321614311421/Holiday-Offers---train-tr-001.jpg")
  val trainsBig = defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/16/1321460655227/Holiday-offers-image---tr-003.jpg")
  val newYorkSmall = defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/18/1321614346487/Holiday-Offers-New-York-001.jpg")
  val newYorkBig = defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/16/1321460714281/Holiday-Offers-image---Ne-003.jpg")
  val indiaSmall = defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/18/1321614378671/Holiday-Offers-India-001.jpg")
  val indiaBig = defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/16/1321460772820/Holiday-offers-image---In-003.jpg")
  val parisSmall = defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/18/1321614408885/Holiday-Offers-Paris-001.jpg")
  val parisBig = defaultOffer("http://static.guim.co.uk/sys-images/Travel/Pix/pictures/2011/11/16/1321460806064/Holiday-Offers-image---Pa-003.jpg")

  val smallDefaults = List(trainsSmall, indiaSmall, newYorkSmall, parisSmall)
  val largeDefaults = List(trainsBig, indiaBig, newYorkBig, parisBig)

  object backgroundColor extends RequestVar[String](throw new IllegalStateException("someone should have set the background color"))

  object campaign extends RequestVar[String](throw new IllegalStateException("someone should have set the campaign"))

  object numTrails extends RequestVar[Int](throw new IllegalStateException("someone should have set the number of offers"))

  object imageSize extends RequestVar[String](throw new IllegalStateException("someone should have set the image size"))

  object offers extends RequestVar[List[Offer]]( getOffersFor (pageUrl) take numTrails.get )

  private def defaultOffers = Random.shuffle(
    Scoped.imageSize.get match {
      case "ThumbOne" => smallDefaults
      case "TwoColumn" => largeDefaults
    }
  )

  private def defaultOffer(image: String) = Offer(-1, None, "http://www.guardianholidayoffers.co.uk/", image,
      "", new DateTime, Nil, Nil)

  private def getOffersFor(pageUrl: String): List[Offer] = {
    val offers = getRealOffersFor(pageUrl) ++ defaultOffers
    if (offers.head.isDefault) {
      campaign.set(campaigns("default"))
    }
    offers
  }

  private def getRealOffersFor(pageUrl: String) =  {
    val apiUrl = "http://content.guardianapis.com/%s?format=xml&show-tags=keyword&api-key=%s".format(pageUrl, apiKey)
    val offers = Appengine.GET(apiUrl) map { xmlString =>

      val keywordFromTagPage = (XML.loadString(xmlString) \ "tag") map { Keyword(_) }
      val keywordsFromPage =  keywordFromTagPage.headOption map { List(_) } getOrElse {
        (XML.loadString(xmlString) \\ "tag") map { Keyword(_) }
      }

      Appengine.getOffers.filter { _.keywords.intersect(keywordsFromPage).size > 0 } sortBy { _.keywords.intersect(keywordsFromPage).size } reverse

    } getOrElse Nil

    Random.shuffle(offers)
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