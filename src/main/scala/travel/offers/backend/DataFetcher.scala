package travel.offers.backend

import net.liftweb.http.rest.RestHelper
import xml.XML
import com.google.appengine.api.taskqueue.TaskOptions.Method
import travel.offers.Scoped._
import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.TaskOptions.Builder._
import com.google.appengine.api.taskqueue.RetryOptions.Builder._
import java.net.URLEncoder
import travel.offers.Appengine._
import travel.offers.{Keyword, Offer}
import net.liftweb.http.S

object DataFetcher extends RestHelper {

  serve {
    case Get("data" :: "refresh" :: Nil, _) => refreshAllOffers
    case Get("data" :: "tags" :: id :: Nil, _) => tagOffer(id.toInt)
  }

  def refreshAllOffers = {
    S.setHeader("Cache-Control", "public, max-age=1")
    val offers = GET("http://extranet.gho.red2.co.uk/Offers/XmlOffers") map { xmlString =>
      val offersFromFeed = (XML.loadString(xmlString) \\ "offer").zipWithIndex.map{ o => Offer(o._2, o._1) }.toList
      cacheRawOffers(offersFromFeed)
      cacheOffers(Nil)
      offersFromFeed
    } getOrElse Nil

    offers foreach { queueForTagging }

    <offers>{offers.size} offers loaded</offers>
  }

  def tagOffer(id: Int) = {
    val taggedOffer = getRawOffers.find(_.id == id) flatMap { oldOffer =>

      val query = "\"" + oldOffer.countries.mkString("\" \"") + "\"".replace("&", "").replace(",", "")

      val apiUrl = "http://content.guardianapis.com/tags?q=%s&format=xml&type=keyword&section=travel&api-key=%s"
            .format(URLEncoder.encode(query, "UTF-8"), apiKey)
      GET(apiUrl).map { xmlString =>
        val keywords: List[Keyword] = ((XML.loadString(xmlString) \\ "tag") map { n => { Keyword((n\"@id").text, (n\"@web-title").text) } }).toList
        tagAndCache(oldOffer, keywords)
      }
    }

    taggedOffer map { offer =>
      <tags>offer {offer.title} tagged with {offer.keywords.mkString(", ")}</tags>
    } getOrElse {
      <tags>offer with id {id} not found</tags>
    }
  }

  private def tagAndCache(oldOffer: Offer, keywords: List[Keyword]) = {
    val newKeywords = (keywords.toList ::: oldOffer.keywords) distinct
    val offersWithKeywords = Offer(oldOffer, newKeywords) :: getOffers
    cacheOffers(offersWithKeywords)
    offersWithKeywords.head
  }

  private def queueForTagging(offer: Offer) = QueueFactory.getDefaultQueue.add(withUrl("/data/tags/" + offer.id)
    .retryOptions(withTaskRetryLimit(5)).method(Method.GET))

}



