package com.asef.dordambdandroid.data.remote.models.prices.editprice


import com.asef.dordambdandroid.data.remote.models.prices.location.PriceLocationFields
import com.asef.dordambdandroid.data.remote.models.prices.location.writeLocation
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/**
 * PATCH /prices/:id. `location` null leaves the price's location untouched
 * (its stored coordinates included); [PriceLocationFields.NONE] removes it.
 */
@JsonAdapter(EditPriceAdapter::class)
data class EditPrice(
    val price: Float = 0.0f,
    val location: PriceLocationFields? = null,
)

// SPEC: spec-628f81
class EditPriceAdapter : TypeAdapter<EditPrice>() {
    override fun write(out: JsonWriter, value: EditPrice?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.beginObject()
        out.name("price").value(value.price)
        value.location?.let { out.writeLocation(it) }
        out.endObject()
    }

    override fun read(reader: JsonReader): EditPrice =
        throw UnsupportedOperationException("EditPrice is only sent, never read")
}
