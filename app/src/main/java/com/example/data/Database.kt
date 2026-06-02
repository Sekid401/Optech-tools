package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dateString: String, // format "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val associatedFileRelativePath: String? = null,
    val associatedCalendarEventId: Int? = null
)

// --- DAOS ---

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE associatedFileRelativePath = :path ORDER BY timestamp DESC")
    fun getNotesForFile(path: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE associatedCalendarEventId = :eventId ORDER BY timestamp DESC")
    fun getNotesForEvent(eventId: Int): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getEventsForDate(dateString: String): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Int)
}

@Dao
interface BrowserDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Int)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    fun isBookmarked(url: String): Flow<Boolean>

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 200")
    fun getHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: HistoryItem): Long

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryItem(id: Int)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}

// --- DATABASE ---

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val category: String, // "Personal", "Work", "Favorite"
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val threadId: String, // phone number or chat identifier
    val sender: String, // "Me" or contact icon-name
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "todo_tasks")
data class TodoTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val priority: String = "Medium", // "High", "Medium", "Low"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_calls")
data class RecentCall(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val contactName: String?,
    val callType: String, // "Outgoing", "Incoming", "Missed"
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Int)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun deleteConversation(threadId: String)
}

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<TodoTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TodoTask): Long

    @Query("UPDATE todo_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateTaskStatus(id: Int, completed: Boolean)

    @Query("DELETE FROM todo_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

@Dao
interface RecentCallDao {
    @Query("SELECT * FROM recent_calls ORDER BY timestamp DESC LIMIT 50")
    fun getRecentCalls(): Flow<List<RecentCall>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentCall(call: RecentCall): Long

    @Query("DELETE FROM recent_calls WHERE id = :id")
    suspend fun deleteRecentCallById(id: Int)

    @Query("DELETE FROM recent_calls")
    suspend fun clearCallHistory()
}

@Database(
    entities = [
        CalendarEvent::class, 
        Bookmark::class, 
        HistoryItem::class, 
        Note::class,
        Contact::class,
        ChatMessage::class,
        TodoTask::class,
        RecentCall::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val calendarDao: CalendarDao
    abstract val browserDao: BrowserDao
    abstract val noteDao: NoteDao
    abstract val contactDao: ContactDao
    abstract val chatDao: ChatDao
    abstract val todoDao: TodoDao
    abstract val recentCallDao: RecentCallDao
}
