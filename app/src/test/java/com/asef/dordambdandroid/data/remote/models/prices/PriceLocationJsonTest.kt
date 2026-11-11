package com.asef.dordambdandroid.data.remote.models.prices

import com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid.AddPriceByItemId
import com.asef.dordambdandroid.data.remote.models.prices.addpricebyitemid.Item
import com.asef.dordambdandroid.data.remote.models.prices.editprice.EditPrice
import com.asef.dordambdandroid.data.remote.models.prices.location.PriceLocationFields
import com.asef.dordambdandroid.data.remote.models.prices.pricebyitemid.PriceByItemIdItem
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// SPEC: spec-5f5a30, spec-14240b, spec-628f81 — the wire format Retrofit's Gson sends.
class PriceLocationJsonTest {

    private val gson = Gson() // the converter's default: nulls are normally dropped

    private fun json(value: Any) = JsonParser.parseString(gson.toJson(value)).asJsonObject

    @Test
    fun addPrice_withoutLocation_sendsOnlyItemAndPrice() {
        val body = json(AddPriceByItemId(item = Item(7), price = 120f))
        assertEquals(setOf("item", "price"), body.keySet())
        assertEquals(7, body["item"].asJsonObject["id"].asInt)
        assertEquals(120.0, body["price"].asDouble, 0.0)
    }

    @Test
    fun addPrice_trimmedLocation_sendsPlaceAndExplicitNullCoordinates() {
        val body = json(AddPriceByItemId(Item(7), 120f, PriceLocationFields(2, null, null, "suggested")))
        assertEquals(2, body["place"].asInt)
        assertTrue(body.has("lat") && body["lat"].isJsonNull)
        assertTrue(body.has("lng") && body["lng"].isJsonNull)
        assertEquals("suggested", body["location_source"].asString)
    }

    @Test
    fun addPrice_pinned_sendsExactCoordinates() {
        val body = json(AddPriceByItemId(Item(7), 120f, PriceLocationFields(null, 23.7412345, 90.4198765, "pinned")))
        assertEquals(23.7412345, body["lat"].asDouble, 0.0)
        assertEquals(90.4198765, body["lng"].asDouble, 0.0)
        assertTrue(body["place"].isJsonNull)
        assertEquals("pinned", body["location_source"].asString)
    }

    @Test
    fun editPrice_untouchedLocation_omitsEveryLocationField() {
        assertEquals(setOf("price"), json(EditPrice(99.5f)).keySet())
    }

    @Test
    fun editPrice_removedLocation_sendsAllFourAsNull() {
        val body = json(EditPrice(99.5f, PriceLocationFields.NONE))
        listOf("place", "lat", "lng", "location_source").forEach {
            assertTrue("$it sent", body.has(it))
            assertTrue("$it null", body[it].isJsonNull)
        }
    }

    @Test
    fun priceRead_withPlace_parsesPathAndSource() {
        val item = gson.fromJson(
            """{"id":5,"price":110,"created_at":"x","updated_at":"y","place_id":3,
               "location_source":"typed","place":{"id":3,"name":"Karwan Bazar","level":"market",
               "source":"osm","path":[{"id":3,"name":"Karwan Bazar","level":"market"},
               {"id":1,"name":"Dhaka","level":"city"}],"label":"Karwan Bazar › Dhaka"}}""",
            PriceByItemIdItem::class.java
        )
        assertEquals(listOf("Karwan Bazar", "Dhaka"), item.place!!.pathOrSelf.map { it.name })
        assertEquals("typed", item.locationSource)
    }

    @Test
    fun priceRead_withoutLocation_hasNone() {
        val item = gson.fromJson("""{"id":5,"price":110,"place":null,"location_source":null}""", PriceByItemIdItem::class.java)
        assertNull(item.place)
        assertNull(item.locationSource)
        assertFalse(item.place != null)
    }
}
