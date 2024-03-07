import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.satellitetracker.models.ListItem
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    private val dbPath: String = context.getDatabasePath(DATABASE_NAME).absolutePath
    private val tableName = "Satelity"

    var columns: Array<String> = arrayOf("NORAD", "Name", "Operator", "User")
    var countries: Array<String> = arrayOf()
    var users: Array<String> = arrayOf()

    companion object {
        private const val TAG = "DBHelper"
        private const val DATABASE_NAME = "Satelity.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = "CREATE TABLE IF NOT EXISTS Satelity (" +
                "NORAD INTEGER NOT NULL," +
                "Name TEXT NOT NULL," +
                "Operator TEXT NOT NULL," +
                "User TEXT NOT NULL)"

        db.execSQL(createTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades here
    }

    fun openDatabase() {
        if (!isDatabaseExists()) {
            copyDatabase()
        }
    }

    private fun isDatabaseExists(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
            checkDB?.close()
        } catch (e: Exception) {
            // Database does not exist yet
        }
        return checkDB != null
    }

    private fun copyDatabase() {
        try {
            val inputStream: InputStream = context.assets.open(DATABASE_NAME)
            val outputStream: OutputStream = FileOutputStream(dbPath)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error copying database", e)
        }
    }

    fun getDatabase(): SQLiteDatabase {

        val dbase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)

        var cursor = dbase.rawQuery("SELECT DISTINCT ${columns[2]} FROM $tableName", null)

        while (cursor.moveToNext()) {
            countries += cursor.getString(cursor.getColumnIndexOrThrow(columns[2]))
        }

        cursor = dbase.rawQuery("SELECT DISTINCT ${columns[3]} FROM $tableName", null)

        while (cursor.moveToNext()) {
            users += cursor.getString(cursor.getColumnIndexOrThrow(columns[3]))
        }

        Log.e("Countries", countries.size.toString())
        Log.e("Users", users.size.toString())

        return dbase
    }

    fun getSatellites(): MutableList<ListItem> {
        val lista = mutableListOf<ListItem>()

        val cursor = this.getDatabase().rawQuery("SELECT * FROM $tableName",null)

        while (cursor.moveToNext()) {
            val norad = cursor.getInt(cursor.getColumnIndexOrThrow(columns[0]))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(columns[1]))
            val operator = cursor.getString(cursor.getColumnIndexOrThrow(columns[2]))
            val user = cursor.getString(cursor.getColumnIndexOrThrow(columns[3]))

            val satData = ListItem(norad, name, operator, user)
            lista.add(satData)
        }

        return lista
    }
}
