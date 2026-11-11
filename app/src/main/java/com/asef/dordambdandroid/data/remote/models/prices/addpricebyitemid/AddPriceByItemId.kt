package com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid


import com.asef.dordambdandroid.data.remote.models.prices.location.PriceLocationFields
import com.asef.dordambdandroid.data.remote.models.prices.location.writeLocation
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/** POST /prices. With no location the body is exactly what it was before locations existed. */
@JsonAdapter(AddPriceByItemIdAdapter::class)
data class AddPriceByItemId(
    val item: Item = Item(),
    val price: Float = 0.0f,
    val location: PriceLocationFields? = null,
)

// SPEC: spec-5f5a30
class AddPriceByItemIdAdapter : TypeAdapter<AddPriceByItemId>() {
    override fun write(out: JsonWriter, value: AddPriceByItemId?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("item").beginObject().name("id").value(value.item.id.toLong()).endObject()
        out.name("price").value(value.price)
        value.location?.let { out.writeLocation(it) }
        out.endObject()
    }

    override fun read(reader: JsonReader): AddPriceByItemId =
        throw UnsupportedOperationException("AddPriceByItemId is only sent, never read")
}
