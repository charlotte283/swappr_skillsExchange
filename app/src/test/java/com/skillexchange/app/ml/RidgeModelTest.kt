package com.skillexchange.app.ml

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks that the Kotlin re-implementation gives the same numbers as scikit-learn's model.predict().
 * test_model.json / test_model_selftest.json are written by ml/export_model.py (here from a small
 * synthetic dataset). Re-copy both from your real export to test the real model.
 */
class RidgeModelTest {
    private fun resource(name: String) =
        javaClass.classLoader!!.getResourceAsStream(name)!!.bufferedReader().use { it.readText() }

    @Test
    fun kotlinMatchesScikitLearn() {
        val model = RidgeModel.fromJson(resource("test_model.json"))
        val checks = JSONArray(resource("test_model_selftest.json"))
        assertTrue(checks.length() > 0)
        for (i in 0 until checks.length()) {
            val c = checks.getJSONObject(i)
            val rating = if (c.isNull("rating")) null else c.getDouble("rating")
            val got = model.estimate(c.getString("service"), c.getString("category"), c.getString("subcat"),
                rating, Math.round(c.getDouble("reviews")).toInt())
            assertEquals(c.getDouble("expected_eur"), got, 1e-3)
        }
    }

    @Test
    fun unknownCategoryDoesNotCrash() {
        val model = RidgeModel.fromJson(resource("test_model.json"))
        val v = model.estimate("fix my bicycle", "Not A Category", "Not A Subcat", null, 0)
        assertTrue(v >= RidgeModel.MIN_VALUE_EUR)
    }
}
