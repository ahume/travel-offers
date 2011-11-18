package travel.offers.backend

import net.liftweb.http.rest.RestHelper
import java.io.OutputStream
import net.liftweb.http.{S, OutputStreamResponse}

object ImageProxy extends RestHelper {
  serve {
    case Get("image" :: Nil, _) => {
      val id = S.param("id").openOr(throw new IllegalArgumentException("pictures must have an id"))
      val imageType = S.param("type").openOr(throw new IllegalArgumentException("pictures must have a type"))

      Appengine.GET_bytes("http://www.guardianholidayoffers.co.uk/Image.aspx?id=%s&type=%s".format(id, imageType)) map {
        bytes:Array[Byte] => OutputStreamResponse((out: OutputStream) =>
          out.write(bytes.toArray), bytes.length, List(("Content-Type", "image/jpeg"), ("Cache-Control", "public, max-age=3600")))
      }
    }
  }
}