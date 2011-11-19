package travel.offers

import com.google.appengine.api.memcache.MemcacheServiceFactory
import java.net.URL
import com.google.appengine.api.urlfetch.{FetchOptions, HTTPMethod, HTTPRequest, URLFetchServiceFactory}
import java.util.logging.Logger

object Appengine extends AppengineLogging {

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
    GET_bytes(url) map { new String(_, "UTF-8") }
  }

  def GET_bytes(url: String): Option[Array[Byte]] = {
   val request = new HTTPRequest(new URL(url), HTTPMethod.GET, FetchOptions.Builder.withDeadline(20.0))
    val response = urlFetcher.fetch(request)
    response.getResponseCode match {
      case 200 => Some(response.getContent)
      case _ => {
        logger.severe("URL fetch failed: " + response.getResponseCode)
        None
      }
    }
  }
}

trait AppengineLogging {

  val logger = Logger.getLogger(getClass.getName)

}