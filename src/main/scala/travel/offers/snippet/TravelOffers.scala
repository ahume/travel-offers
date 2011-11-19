package travel.offers.snippet

import net.liftweb.util._
import Helpers._
import travel.offers.Scoped._
import xml.Unparsed

class TravelOffers {

  def color = ".travel-offers [class+]" #> backgroundColor.get

  def links = ".browse-all [href]" #> ("http://www.guardianholidayoffers.co.uk?INTCMP=" + campaign.get) &
    ".email-signup [href]" #> ("http://www.guardianholidayoffers.co.uk/email-sign-up?INTCMP=" + campaign.get)
    ".browse-all [class]" #> "" &
    ".email-signup [class]" #> ""


  def search = {
    val offer = offers.get.head
    ("#travel-offer [value]" #> (offer.keywords.headOption map { _.name } getOrElse (""))) &
    ("name=INTCMP [value]" #> campaign.get)
  }

  def trail = {
    "*" #> (offers.get.zip(trailClasses) map { case(offer, trailClass) =>
      (".trail [class]"      #> trailClass) &
      (".offer-image [src]"  #> offer.imageUrl) &
      (".link [href]"        #> offer.offerUrl) &
      (".offer-image [width]" #> imageWidthInPixels) &
      (".offer-image [alt]"  #> offer.title.getOrElse("Find hand picked holidays")) &
      (offer.title map { title => ".lift-offer *" #> Unparsed(title + " - from &pound;" + offer.fromPrice) } getOrElse { ".trail-text" #> "" }) &
      (".lift-offer [class]" #> "link")
    })
  }

  private def trailClasses = numTrails.get match {
    case 1 => List("")
    case 2 => List("two-col", "two-col edge")
  }

  private def imageWidthInPixels = imageSize.get match {
    case "TwoColumn" => "280px"
    case "ThumbOne" =>  "140px"
  }
}
