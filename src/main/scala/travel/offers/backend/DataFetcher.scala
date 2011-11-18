package travel.offers.backend

import net.liftweb.http.rest.RestHelper
import xml.{Node, XML}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.google.appengine.api.taskqueue.TaskOptions.Method
import travel.offers.Scoped
import com.google.appengine.api.taskqueue.{RetryOptions, TaskOptions, QueueFactory}
import com.google.appengine.api.memcache.MemcacheServiceFactory
import java.net.{URL, URLEncoder}
import com.google.appengine.api.urlfetch.{FetchOptions, HTTPMethod, HTTPRequest, URLFetchServiceFactory}

object DataFetcher extends RestHelper {

  serve {
    case Get("data" :: "refresh" :: Nil, _) => refresh

    case Get("data" :: "tags" :: id :: Nil, _) => tags(id.toInt)
  }

  def refresh = {
    val offers = Appengine.GET("http://extranet.gho.red2.co.uk/Offers/XmlOffers") map { xmlString =>
      val o: List[Offer] = (XML.loadString(xmlString) \\ "offer").zipWithIndex.map{ o => Offer(o._2, o._1) }.toList
      Appengine.cacheRawOffers(o)
      Appengine.cacheOffers(Nil)
      o
    } getOrElse Nil

    offers foreach {
      o =>
        val task: TaskOptions = TaskOptions.Builder.withUrl("/data/tags/" + o.id)
          .retryOptions(RetryOptions.Builder.withTaskRetryLimit(5)).method(Method.GET)
        QueueFactory.getDefaultQueue.add(task)
    }

    <ok/>
  }

  def tags(id: Int) = {
    getOfferFromRaw(id) foreach { oldOffer =>

      val query = "\"" + oldOffer.countries.mkString("\" \"") + "\"".replace("&", "").replace(",", "")

      val apiUrl = "http://content.guardianapis.com/tags?q=%s&format=xml&type=keyword&section=travel&api-key=%s"
            .format(URLEncoder.encode(query, "UTF-8"), Scoped.apiKey)
      Appengine.GET(apiUrl).foreach { xmlString =>
        val keywords = (XML.loadString(xmlString) \\ "tag") map { n => { Keyword((n\"@id").text, (n\"@web-title").text) } }
        val newKeywords = (keywords.toList ::: oldOffer.keywords) distinct
        val newOffer: Offer = Offer(oldOffer, newKeywords)
        val offersWithKeywords = newOffer :: Appengine.getOffers
        Appengine.cacheOffers(offersWithKeywords)
      }
    }
    <ok/>
  }

  private def getOfferFromRaw(id: Int): Option[Offer] = Appengine.getRawOffers.find(_.id == id)

}

case class Keyword(id: String, name: String)

object Keyword {
  def apply(node: Node): Keyword = Keyword((node \\ "@id").text, (node \\ "@web-title").text)
}

case class Offer(id: Int, title: Option[String], offerUrl: String, private val _imageUrl: String, fromPrice: String,
                 earliestDeparture: DateTime, keywords: List[Keyword], countries: List[String]) {

  //this needs to be a def - do NOT val or lazy val it
  def imageUrl = {
    if (_imageUrl contains "type=") {
      val end = _imageUrl.lastIndexOf("type=") + 5
      _imageUrl.substring(0, end) + Scoped.imageSize.get + "&INTCMP=" + Scoped.campaign.get
    } else { _imageUrl }
  }
}

object Offer {

  val dateFormat = DateTimeFormat.forPattern("dd-MMM-yyyy")

  def apply(o: Offer, keywords: List[Keyword]): Offer = Offer(o.id, o.title, o.offerUrl, o._imageUrl, o.fromPrice, o.earliestDeparture, keywords, o.countries)

  def apply(id: Int, node: Node): Offer = {
    Offer(
      id,
      Some((node \\ "title") text),
      (node \\ "offerurl") text,
      (node \\ "imageurl").text replace("NoResize", "ThreeColumn"),
      (node \ "@fromprice").text.replace(".00", ""),
      dateFormat.parseDateTime((node \ "@earliestdeparture").text),
      Nil,
      (node \\ "country") map { _.text } toList
    )
  }
}

object Appengine {

  private val cache = MemcacheServiceFactory.getMemcacheService

  private val urlFetcher = URLFetchServiceFactory.getURLFetchService


  def cacheRawOffers(offers: List[Offer]) {
    cache.put("raw-offers", offers)
  }

  def getRawOffers = Option(cache.get("raw-offers")) map  { case offers: List[Offer] => offers } getOrElse Nil

  def cacheOffers(offers: List[Offer]) {
    cache.put("offers", offers)
  }

  def getOffers = Option(cache.get("offers")) map  { case offers: List[Offer] => offers } getOrElse Nil

  def GET(url: String): Option[String] = {

    val request = new HTTPRequest(new URL(url), HTTPMethod.GET, FetchOptions.Builder.withDeadline(20.0))
    val response = urlFetcher.fetch(request)
    response.getResponseCode match {
      case 200 => new Some(new String(response.getContent))
      case _ => None
    }
  }
}