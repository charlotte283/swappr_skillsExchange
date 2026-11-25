package com.skillexchange.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Keeps the user's own profile, credentials database, services and requests on the phone (SharedPreferences + JSON). */
class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("skill_exchange", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = !prefs.getString("active_user_email", null).isNullOrBlank()

    fun getActiveEmail(): String? = prefs.getString("active_user_email", null)

    fun setActiveEmail(email: String?) {
        if (email == null) {
            prefs.edit().remove("active_user_email").apply()
        } else {
            prefs.edit().putString("active_user_email", email.trim().lowercase()).apply()
        }
    }

    /** Save user credentials to database */
    fun registerUser(email: String, pass: String, p: UserProfile): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || pass.isBlank()) return false
        val dbStr = prefs.getString("user_db", "{}") ?: "{}"
        val db = try { JSONObject(dbStr) } catch (_: Exception) { JSONObject() }

        if (db.has(cleanEmail)) return false // Email already exists

        val userObj = JSONObject()
            .put("email", cleanEmail)
            .put("password", pass)
            .put("name", p.name)
            .put("area", p.area)
            .put("bio", p.bio)
            .put("profilePhoto", p.profilePhoto)

        db.put(cleanEmail, userObj)
        prefs.edit().putString("user_db", db.toString()).apply()
        saveProfile(p)
        setActiveEmail(cleanEmail)
        return true
    }

    /** Verify credentials from database */
    fun loginUser(email: String, pass: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val dbStr = prefs.getString("user_db", null) ?: return false
        return try {
            val db = JSONObject(dbStr)
            if (!db.has(cleanEmail)) return false
            val userObj = db.getJSONObject(cleanEmail)
            val storedPass = userObj.getString("password")
            if (storedPass == pass) {
                // Restore user profile
                val p = UserProfile(
                    name = userObj.optString("name", "User"),
                    area = userObj.optString("area", "Kilimani"),
                    bio = userObj.optString("bio", "Passionate learner & skill exchanger."),
                    profilePhoto = userObj.optString("profilePhoto", "")
                )
                saveProfile(p)
                setActiveEmail(cleanEmail)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun logout() {
        setActiveEmail(null)
    }

    fun saveProfile(p: UserProfile) {
        val filesArr = JSONArray()
        p.uploadedFiles.forEach { file ->
            filesArr.put(
                JSONObject()
                    .put("name", file.name)
                    .put("uri", file.uri)
                    .put("size", file.size)
                    .put("mimeType", file.mimeType)
            )
        }

        val o = JSONObject()
            .put("name", p.name)
            .put("area", p.area)
            .put("bio", p.bio)
            .put("wants", JSONArray(p.wants.toList()))
            .put("profilePhoto", p.profilePhoto)
            .put("photos", JSONArray(p.photos))
            .put("uploadedFiles", filesArr)
        prefs.edit().putString("profile", o.toString()).apply()
    }

    fun loadProfile(): UserProfile {
        val text = prefs.getString("profile", null) ?: return UserProfile()
        return try {
            val o = JSONObject(text)
            val w = o.getJSONArray("wants")
            val pArr = o.optJSONArray("photos")
            val photos = if (pArr != null) List(pArr.length()) { pArr.getString(it) } else emptyList()

            val fArr = o.optJSONArray("uploadedFiles")
            val files = if (fArr != null) List(fArr.length()) {
                val fObj = fArr.getJSONObject(it)
                UploadedFile(
                    name = fObj.optString("name", "File ${it + 1}"),
                    uri = fObj.optString("uri", ""),
                    size = fObj.optString("size", ""),
                    mimeType = fObj.optString("mimeType", "")
                )
            } else emptyList()

            UserProfile(
                name = o.optString("name", "You"),
                area = o.optString("area", "Kilimani"),
                bio = o.optString("bio", "Passionate learner & skill exchanger."),
                wants = List(w.length()) { w.getString(it) }.toSet(),
                profilePhoto = o.optString("profilePhoto", ""),
                photos = photos,
                uploadedFiles = files
            )
        } catch (e: Exception) {
            UserProfile()
        }
    }

    fun saveServices(list: List<Service>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("title", it.title).put("category", it.category)
                .put("subcat", it.subcat).put("value", it.valueEur))
        }
        prefs.edit().putString("services", arr.toString()).apply()
    }

    fun loadServices(): List<Service> {
        val text = prefs.getString("services", null) ?: return emptyList()
        return try {
            val arr = JSONArray(text)
            List(arr.length()) {
                val o = arr.getJSONObject(it)
                Service(o.getString("id"), "me", o.getString("title"), o.getString("category"),
                    o.getString("subcat"), o.getDouble("value"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveRequests(list: List<ServiceRequest>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("providerId", it.providerId).put("providerName", it.providerName)
                .put("theirTitle", it.theirTitle).put("theirValue", it.theirValueEur).put("type", it.type.name)
                .put("myTitle", it.myTitle ?: JSONObject.NULL).put("myValue", it.myValueEur)
                .put("topUp", it.topUpEur).put("status", it.status.name).put("myRating", it.myRating ?: JSONObject.NULL))
        }
        prefs.edit().putString("requests", arr.toString()).apply()
    }

    fun loadRequests(): List<ServiceRequest> {
        val text = prefs.getString("requests", null) ?: return emptyList()
        return try {
            val arr = JSONArray(text)
            List(arr.length()) {
                val o = arr.getJSONObject(it)
                ServiceRequest(
                    id = o.getString("id"), providerId = o.getString("providerId"), providerName = o.getString("providerName"),
                    theirTitle = o.getString("theirTitle"), theirValueEur = o.getDouble("theirValue"),
                    type = RequestType.valueOf(o.getString("type")),
                    myTitle = if (o.isNull("myTitle")) null else o.getString("myTitle"),
                    myValueEur = o.getDouble("myValue"), topUpEur = o.getDouble("topUp"),
                    status = RequestStatus.valueOf(o.getString("status")),
                    myRating = if (o.isNull("myRating")) null else o.getInt("myRating"),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
