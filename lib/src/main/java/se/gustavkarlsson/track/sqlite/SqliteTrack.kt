package se.gustavkarlsson.track.sqlite

import android.database.Cursor
import se.gustavkarlsson.track.Record
import se.gustavkarlsson.track.Track

internal class SqliteTrack(
    private val sqlite: Sqlite,
    private val appVersion: Long,
    private val getTimestamp: () -> Long = System::currentTimeMillis,
    private val readOptionalRecord: Cursor.() -> Record? = Cursor::readOptionalRecord,
    private val toRecordSequence: Cursor.() -> Sequence<Record> = Cursor::toRecordSequence
) : Track {
    override fun get(key: String): Record? {
        val selections = listOf(
            Selection(Table.COLUMN_KEY, Operator.Equals, key),
            Selection(Table.COLUMN_SINGLETON, Operator.Equals, true)
        )
        return sqlite.query(selections, limit = 1) { it.readOptionalRecord() }
    }

    override fun set(key: String, value: String): Boolean {
        val selections = listOf(
            Table.COLUMN_KEY isEqualTo key,
            Table.COLUMN_SINGLETON isEqualTo true
        )
        return sqlite.upsert(selections, createRow(key, value, true))
    }

    override fun <T> query(
        key: String,
        selector: (Sequence<Record>) -> T
    ): T {
        val selections = listOf(Table.COLUMN_KEY isEqualTo key)
        return sqlite.query(selections) { selector(it.toRecordSequence()) }
    }

    override fun add(key: String, value: String) {
        sqlite.insert(createRow(key, value, false))
    }

    private fun createRow(key: String, value: String, singleton: Boolean): Map<String, Any> =
        mapOf(
            Table.COLUMN_SINGLETON to singleton,
            Table.COLUMN_KEY to key,
            Table.COLUMN_TIMESTAMP to getTimestamp(),
            Table.COLUMN_APP_VERSION to appVersion,
            Table.COLUMN_VALUE to value
        )

    override fun remove(id: Long): Boolean {
        val selections = listOf(Table.COLUMN_ID isEqualTo id)
        return sqlite.delete(selections) > 0
    }

    override fun remove(key: String): Int {
        val selections = listOf(Table.COLUMN_KEY isEqualTo key)
        return sqlite.delete(selections)
    }

    override fun remove(selector: (Record) -> Boolean): Int {
        val ids = sqlite.query(emptyList()) {
            it.toRecordSequence()
                .filter(selector)
                .map(Record::id)
                .toList()
        }
        val selections = listOf(Table.COLUMN_ID isIn ids)
        return sqlite.delete(selections)
    }

    override fun deleteDatabase() = sqlite.deleteDatabase()
}
