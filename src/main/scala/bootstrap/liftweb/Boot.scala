package bootstrap.liftweb

import net.liftweb.common.Empty
import net.liftweb.http._
import net.liftweb.util.{NamedPF, Helpers}
import rest.RestHelper
import travel.offers.Scoped._
import util.Random
import java.lang.IllegalStateException
import travel.offers.Scoped
import travel.offers.backend.{OffersList, ImageProxy, Appengine, DataFetcher}

class Boot {

  object rand extends Random

  def boot {

    LiftRules.htmlProperties.default.set((r: Req) => (new OldHtmlProperties(r.userAgent)).setHtmlOutputHeader(() => Empty).setDocType(() => Empty))

    LiftRules.addToPackages("travel.offers")

    LiftRules.early.append(r => r.setCharacterEncoding("UTF-8"))

    LiftRules.defaultHeaders = {
     case _ =>
       //todo set max age to something sensible
        List("Date" -> Helpers.nowAsInternetDate,
          "Cache-Control" -> "public, max-age=1",
          "Content-Type" -> "text/html; charset=UTF-8")
    }

    LiftRules.autoIncludeAjax = _ => false

    LiftRules.autoIncludeComet = _ => false

    //these are needed to work in Appengine
    LiftRules.enableContainerSessions = false
    LiftRules.getLiftSession = req => new LiftSession(req.contextPath, "dummySession", Empty)
    LiftRules.sessionCreator = (i1, i2) => error("no sessions here please")

    //all paths are stateless (removes lift_page JS on bottom of page
    LiftRules.statelessTest.prepend({case _ => true})

    LiftRules.statelessDispatchTable.append(DataFetcher)
      .append(ImageProxy)
      .append(OffersList)

    LiftRules.statelessRewrite.prepend(NamedPF("components") {
      case RewriteRequest(ParsePath("narrow" :: Nil, _, _, _), _, _) => {
        numTrails.set(1)
        imageSize.set("ThumbOne")

        (rand.nextInt(2)) match {
          case 0 => {
            backgroundColor.set("variant-color-grey")
            campaign.set(campaigns("narrow-grey"))
          }
          case 1 => {
            backgroundColor.set("variant-color-blue")
            campaign.set(campaigns("narrow-blue"))
          }
        }

        RewriteResponse("travelOffers" :: Nil, Map.empty[String, String])
      }

      case RewriteRequest(ParsePath("promo" :: Nil, _, _, _), _, _) => {
        numTrails.set( rand.nextInt(2) + 1)
        numTrails.get match {
          case 1 => {
            imageSize.set("TwoColumn")
            campaign.set(campaigns("promo-1-offer"))
          }
          case 2 => {
            imageSize.set("ThumbOne")
            campaign.set(campaigns("promo-2-offer"))
          }
          case _ => throw new IllegalStateException("there should only be 1 or 2 trails")
        }
        RewriteResponse("travelOffersPromo" :: Nil, Map.empty[String, String])
      }
    })
  }
}