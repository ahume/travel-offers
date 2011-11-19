package travel.offers

import xml.Node
import org.joda.time.DateTime
import travel.offers.Scoped.{campaign, imageSize}
import org.joda.time.format.DateTimeFormat

case class Keyword(id: String, name: String)

object Keyword {
  def apply(node: Node): Keyword = Keyword((node \\ "@id").text, (node \\ "@web-title").text)
}

case class Offer(id: Int, title: Option[String], _offerUrl: String, private val _imageUrl: String, fromPrice: String,
                 earliestDeparture: DateTime, keywords: List[Keyword], countries: List[String]) {

  //this needs to be a def - do NOT val or lazy val it
  def offerUrl = _offerUrl + intCmp

  def imageUrl = {
    if (_imageUrl contains "type=") {
      val end = _imageUrl.lastIndexOf("type=") + 5
      _imageUrl.substring(0, end) + imageSize.get + intCmp
    } else { _imageUrl }
  }

  private def intCmp = "&INTCMP=" + campaign.get
}

object Offer {

  val dateFormat = DateTimeFormat.forPattern("dd-MMM-yyyy")

  def apply(o: Offer, keywords: List[Keyword]): Offer = Offer(o.id, o.title, o._offerUrl, o._imageUrl, o.fromPrice, o.earliestDeparture, keywords, o.countries)

  def apply(id: Int, node: Node): Offer = {
    Offer(
      id,
      Some((node \\ "title") text),
      (node \\ "offerurl") text,
      (node \\ "imageurl").text.replace("NoResize", "ThreeColumn").replace("http://www.guardianholidayoffers.co.uk/Image.aspx", "http://resource.guim.co.uk/travel/holiday-offers-micro/image"),
      (node \ "@fromprice").text.replace(".00", ""),
      dateFormat.parseDateTime((node \ "@earliestdeparture").text),
      Nil,
      (node \\ "country") map { _.text } toList
    )
  }
}