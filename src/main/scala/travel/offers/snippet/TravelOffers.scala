package travel.offers.snippet

import net.liftweb.util._
import Helpers._
import travel.offers.Scoped

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
      (".offer-image [alt]"  #> offer.title) &
      (".lift-offer *"       #> offer.title ) &
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
