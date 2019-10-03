package se.gustavkarlsson.track

import android.content.Context
import android.os.Build
import se.gustavkarlsson.track.sqlite.Sqlite
import se.gustavkarlsson.track.sqlite.SqliteTrack

/**
 * A simple database holding [Record]s.
 */
interface Track {
    /**
     * Gets a singleton record for [key] if one exists. Will not return records added with [add].
     */
    fun get(key: String): Record?

    /**
     * Sets a singleton value for [key]. Overwrites any existing value.
     *
     * @return `true` if an existing value was replaced, otherwise `false`
     */
    fun set(key: String, value: String = ""): Boolean

    /**
     * Gets all records for [key].
     */
    fun query(key: String): List<Record> = query(key) { it.toList() }

    /**
     * Gets a sequence all records for [key] and invokes [selector] on the resulting sequence,
     * yielding a return value.
     *
     * **Caution:** Do not leak references to the sequence as it's backing data source may
     * not be available after this function returns.
     */
    fun <T> query(key: String, selector: (Sequence<Record>) -> T): T

    /**
     * Adds a [value] for [key]
     */
    fun add(key: String, value: String = "")

    /**
     * Removes the record matching [id] if it exists.
     * Returns `true` if any record was removed, otherwise `false`.
     */
    fun remove(id: Long): Boolean

    /**
     * Removes all records matching [key] and returns the number of records removed.
     */
    fun remove(key: String): Int

    /**
     * Removes all records matching [filter] and returns the number of records removed.
     */
    fun remove(filter: (Record) -> Boolean): Int

    /**
     * Clears all records and closes any underlying resources.
     * Returns `true` if successful, otherwise `false`.
     */
    fun clear(): Boolean

    /**
     * The default instance of [Track]. Must be initialized with [initialize] before use.
     */
    companion object : Track {
        private var initializedDelegate: Track? = null
        private inline val delegate: Track
            get() = requireNotNull(initializedDelegate) {
                "Track is not yet initialized. Run initialize() first"
            }

        /**
         * Initializes this instance using [context].
         */
        fun initialize(context: Context) {
            initializedDelegate = SqliteTrack(Sqlite(context), context.appVersion)
        }

        override fun get(key: String) = delegate.get(key)

        override fun set(key: String, value: String) = delegate.set(key, value)

        override fun <T> query(
            key: String,
            selector: (Sequence<Record>) -> T
        ) = delegate.query(key, selector)

        override fun add(key: String, value: String) = delegate.add(key, value)

        override fun remove(id: Long) = delegate.remove(id)

        override fun remove(key: String) = delegate.remove(key)

        override fun remove(filter: (Record) -> Boolean) = delegate.remove(filter)

        override fun clear() = delegate.clear()
    }
}

private val Context.appVersion: Long
    get() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }
