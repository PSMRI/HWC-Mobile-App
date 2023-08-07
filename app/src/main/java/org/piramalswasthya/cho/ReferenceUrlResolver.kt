package org.piramalswasthya.cho

import android.graphics.BitmapFactory
import org.hl7.fhir.r4.model.Binary
import android.content.Context
import android.graphics.Bitmap
import com.google.android.fhir.datacapture.UrlResolver
import com.google.android.fhir.get
import org.hl7.fhir.r4.model.ResourceType

class ReferenceUrlResolver(val context: Context) : UrlResolver {

  override suspend fun resolveBitmapUrl(url: String): Bitmap? {
    val logicalId = getLogicalIdFromFhirUrl(url, ResourceType.Binary)
    val binary = CHOApplication.fhirEngine(context).get<Binary>(logicalId)
    return BitmapFactory.decodeByteArray(binary.data, 0, binary.data.size)
  }
}

private fun getLogicalIdFromFhirUrl(url: String, resourceType: ResourceType): String {
  return url.substringAfter("${resourceType.name}/").substringBefore("/")
}