package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val calendarDao: CalendarDao,
    private val browserDao: BrowserDao,
    private val noteDao: NoteDao,
    private val contactDao: ContactDao,
    private val chatDao: ChatDao,
    private val todoDao: TodoDao,
    private val recentCallDao: RecentCallDao
) {
    // --- Contact Operations ---
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    suspend fun insertContact(contact: Contact): Long = contactDao.insertContact(contact)
    suspend fun deleteContactById(id: Int) = contactDao.deleteContactById(id)

    // --- Message/Chat Operations ---
    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()
    fun getMessagesForThread(threadId: String): Flow<List<ChatMessage>> = chatDao.getMessagesForThread(threadId)
    suspend fun insertMessage(message: ChatMessage): Long = chatDao.insertMessage(message)
    suspend fun deleteConversation(threadId: String) = chatDao.deleteConversation(threadId)

    // --- Task Operations ---
    val allTasks: Flow<List<TodoTask>> = todoDao.getAllTasks()
    suspend fun insertTask(task: TodoTask): Long = todoDao.insertTask(task)
    suspend fun updateTaskStatus(id: Int, completed: Boolean) = todoDao.updateTaskStatus(id, completed)
    suspend fun deleteTaskById(id: Int) = todoDao.deleteTaskById(id)

    // --- Recent Call Operations ---
    val recentCalls: Flow<List<RecentCall>> = recentCallDao.getRecentCalls()
    suspend fun insertRecentCall(call: RecentCall): Long = recentCallDao.insertRecentCall(call)
    suspend fun deleteRecentCallById(id: Int) = recentCallDao.deleteRecentCallById(id)
    suspend fun clearCallHistory() = recentCallDao.clearCallHistory()

    // --- Note Operations ---
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun getNotesForFile(path: String): Flow<List<Note>> =
        noteDao.getNotesForFile(path)

    fun getNotesForEvent(eventId: Int): Flow<List<Note>> =
        noteDao.getNotesForEvent(eventId)

    suspend fun insertNote(note: Note): Long =
        noteDao.insertNote(note)

    suspend fun deleteNoteById(id: Int) =
        noteDao.deleteNoteById(id)

    // --- Calendar Operations ---
    val allEvents: Flow<List<CalendarEvent>> = calendarDao.getAllEvents()

    fun getEventsForDate(dateString: String): Flow<List<CalendarEvent>> =
        calendarDao.getEventsForDate(dateString)

    suspend fun insertEvent(event: CalendarEvent): Long =
        calendarDao.insertEvent(event)

    suspend fun deleteEventById(id: Int) =
        calendarDao.deleteEventById(id)

    // --- Browser Operations ---
    val allBookmarks: Flow<List<Bookmark>> = browserDao.getAllBookmarks()
    val allHistory: Flow<List<HistoryItem>> = browserDao.getHistory()

    suspend fun insertBookmark(bookmark: Bookmark): Long =
        browserDao.insertBookmark(bookmark)

    suspend fun deleteBookmarkById(id: Int) =
        browserDao.deleteBookmarkById(id)

    suspend fun deleteBookmarkByUrl(url: String) =
        browserDao.deleteBookmarkByUrl(url)

    fun isBookmarked(url: String): Flow<Boolean> =
        browserDao.isBookmarked(url)

    suspend fun insertHistoryItem(item: HistoryItem): Long =
        browserDao.insertHistoryItem(item)

    suspend fun deleteHistoryItem(id: Int) =
        browserDao.deleteHistoryItem(id)

    suspend fun clearHistory() =
        browserDao.clearHistory()
}
