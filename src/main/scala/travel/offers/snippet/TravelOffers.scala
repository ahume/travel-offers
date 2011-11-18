package travel.offers.snippet

import net.liftweb.util._
import Helpers._
import travel.offers.Scoped
import xml.Unparsed

class TravelOffers {

  def color = ".travel-offers [class+]" #> Scoped.backgroundColor.get

  def search = {
    val offer = Scoped.offers.get.head
    "#travel-offer [value]" #> (offer.keywords.headOption map { _.name } getOrElse (""))
  }

  def trail = {
    "*" #> (Scoped.offers.get.zip(trailClasses) map { case(offer, trailClass) =>
      (".trail [class]"      #> trailClass) &
      (".offer-image [src]"  #> offer.imageUrl) &
      (".link [href]"        #> offer.offerUrl) &
      (".offer-image [width]" #> imageWidthInPixels) &
      (".offer-image [alt]"  #> offer.title.getOrElse("Find hand picked holidays")) &
      (offer.title map { title => ".lift-offer *" #> Unparsed(title + " - from &pound;" + offer.fromPrice) } getOrElse { ".trail-text" #> "" }) &
      (".lift-offer [class]" #> "link")
    })
  }

  private def trailClasses = Scoped.numTrails.get match {
    case 1 => List("")
    case 2 => List("two-col", "two-col edge")
  }

  private def imageWidthInPixels = Scoped.imageSize.get match {
    case "TwoColumn" => "280px"
    case "ThumbOne" =>  "140px"
  }
}
