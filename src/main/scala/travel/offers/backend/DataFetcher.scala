package travel.offers.backend

import net.liftweb.http.rest.RestHelper
import xml.{Node, XML}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.google.appengine.api.taskqueue.{TaskOptions, QueueFactory}
import java.net.URLEncoder
import appenginehelpers.{ExpirationSeconds, HybridCache, Response, UrlFetcher}
import com.google.appengine.api.taskqueue.TaskOptions.Method

object DataFetcher extends RestHelper with UrlFetcher with HybridCache {

  serve {
    case Get("data" :: "refresh" :: Nil, _) => refresh
    case Get("data" :: "tags" :: id :: Nil, _) => tags(id.toInt)
  }

  def refresh = {
    val offers = GET("http://extranet.gho.red2.co.uk/Offers/XmlOffers", None, ExpirationSeconds(0)) match {
      case Response(200, Some(xmlString), _) => {

        val offers: List[Offer] = (XML.loadString(xmlString) \\ "offer").zipWithIndex.map {
          o => Offer(o._2, o._1)
        } toList

        cache.put("offers", offers)

        offers
      }
    }

    offers foreach {
      o =>
        val task: TaskOptions = TaskOptions.Builder.withUrl("/data/tags/" + o.id).method(Method.GET)
        QueueFactory.getDefaultQueue.add(task)
    }

      <ok/>
  }

  def tags(id: Int) = {

    cache.get("offers") match {
      case Some(offers: List[Offer]) => {
        val offer = offers filter {
          (o: Offer) => o.id == id
        } headOption

        offer foreach { o =>
          val apiUrl = "http://content.guardianapis.com/tags?q=%s&format=xml&type=keyword&section=travel"
            .format(URLEncoder.encode(o.title.replace("&", "").replace(",", ""), "UTF-8"))
          GET(apiUrl, None, ExpirationSeconds(30 * 60)) match {
            case Response(200, Some(xmlString), _) => {
              val keywords = (XML.loadString(xmlString) \\ "tag") map { Keyword(_) }
              val newKeywords = (keywords.toList ::: o.keywords) distinct
              val newOffer: Offer = Offer(o, newKeywords)
              val newOffers: List[Offer] = newOffer :: (offers.filterNot(_.id == o.id))
              cache.put("offers", newOffers.toList)
              println(newOffer)
            }
          }
        }

        <ok/>
      }
      case a => <not_ok/>
    }


  }

}

case class Keyword(id: String)

object Keyword {
  def apply(node: Node): Keyword = Keyword((node \\ "@id").text)
}

case class Offer(id: Int, title: String, offerUrl: String, imageUrl: String, fromPrice: String, earliestDeparture: DateTime, keywords: List[Keyword])

object Offer {

  val dateFormat = DateTimeFormat.forPattern("dd-MMM-yyyy")

  def apply(o: Offer, keywords: List[Keyword]): Offer = Offer(o.id, o.title, o.offerUrl, o.imageUrl, o.fromPrice, o.earliestDeparture, keywords)

  def apply(id: Int, node: Node): Offer = {
    Offer(
      id,
      (node \\ "title") text,
      (node \\ "offerurl") text,
      (node \\ "imageurl").text replace("NoResize", "ThreeColumn"),
      (node \ "@fromprice").text,
      dateFormat.parseDateTime((node \ "@earliestdeparture").text),
      Nil
    )
  }
}