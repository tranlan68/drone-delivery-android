# Database Module - Room Integration

Module này cung cấp Room database implementation cho dự án, kết hợp với API calls để tạo cache layer hiệu quả.

## 🏗️ Cấu trúc Module

```
libraries/database/
├── src/main/java/com/delivery/database/
│   ├── entity/           # Room entities
│   │   ├── SongEntity.kt
│   │   ├── AlbumEntity.kt
│   │   └── ArtistEntity.kt
│   ├── dao/             # Data Access Objects
│   │   ├── SongDao.kt
│   │   ├── AlbumDao.kt
│   │   └── ArtistDao.kt
│   ├── converter/       # Type converters
│   │   └── DateConverter.kt
│   ├── repository/      # Repository implementations
│   │   └── MusicRepository.kt
│   ├── module/          # Hilt DI modules
│   │   └── DatabaseModule.kt
│   └── AppDatabase.kt   # Room database class
```

## 🚀 Cách sử dụng

### 1. Basic Usage trong Repository

```kotlin
@Singleton
class HomeRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val musicRepository: MusicRepository
) : BaseRepository() {

    // Offline-first approach
    fun getDataOfflineFirst() = musicRepository.getHomeDataOfflineFirst()
    
    // Cache-first with API fallback
    fun getDataWithCache() = musicRepository.getHomeDataWithCache()
}
```

### 2. Usage trong ViewModel

```kotlin
@HiltViewModel
class HomePageViewModel @Inject constructor(
    private val repository: HomeRepository
) : BaseViewModel() {

    private fun loadData() {
        repository.getDataOfflineFirst()
            .flowOn(Dispatchers.IO)
            .onStart { isLoading.value = true }
            .onCompletion { isLoading.value = false }
            .onEach { listHomePage.value = it }
            .catch { messageError.value = it.message }
            .launchIn(viewModelScope)
    }
    
    fun refreshData() {
        repository.getDataWithCache()
            .flowOn(Dispatchers.IO)
            .onStart { isLoading.value = true }
            .onCompletion { isLoading.value = false }
            .onEach { listHomePage.value = it }
            .catch { messageError.value = it.message }
            .launchIn(viewModelScope)
    }
}
```

## 📊 Caching Strategies

### 1. **Offline-First**
```kotlin
fun getHomeDataOfflineFirst() = flow {
    // 1. Emit cached data first (if available)
    val cachedData = getCachedData()
    if (cachedData.isNotEmpty()) {
        emit(cachedData)
    }
    
    // 2. Then try to fetch from API and update cache
    try {
        val freshData = fetchFromApi()
        saveToCache(freshData)
        emit(freshData)
    } catch (e: Exception) {
        // API failed, but we already emitted cached data
    }
}
```

### 2. **Cache-First with API Fallback**
```kotlin
fun getHomeDataWithCache() = flow {
    try {
        // Try API first and cache result
        val apiData = fetchFromApi()
        saveToCache(apiData)
        emit(apiData)
    } catch (e: Exception) {
        // Fallback to cached data
        val cachedData = getCachedData()
        emit(cachedData)
    }
}
```

## 🔧 Database Operations

### Songs
```kotlin
// Get all songs
val songs: Flow<List<Song>> = musicRepository.getAllSongs()

// Search songs
val searchResults: Flow<List<Song>> = musicRepository.searchSongs("query")

// Sync from API
val freshSongs: List<Song> = musicRepository.syncSongsFromApi()
```

### Albums
```kotlin
// Get all albums
val albums: Flow<List<Album>> = musicRepository.getAllAlbums()

// Get album by ID
val album: Album? = musicRepository.getAlbumById("albumId")

// Sync from API
val freshAlbums: List<Album> = musicRepository.syncAlbumsFromApi()
```

### Artists
```kotlin
// Get all artists
val artists: Flow<List<Artist>> = musicRepository.getAllArtists()

// Search artists
val searchResults: Flow<List<Artist>> = musicRepository.searchArtists("query")

// Sync from API
val freshArtists: List<Artist> = musicRepository.syncArtistsFromApi()
```

## 🧹 Cache Management

```kotlin
// Clear all cache
musicRepository.clearCache()

// Clear old cache (older than 7 days)
musicRepository.clearOldCache(olderThanDays = 7)
```

## 📝 Entity Mapping

Module này sử dụng extension functions để convert giữa network models và database entities:

```kotlin
// Convert to entity
val songEntity = song.toEntity()

// Convert to model
val song = songEntity.toModel()
```

## 🔒 Database Schema

### Songs Table
```sql
CREATE TABLE songs (
    id TEXT PRIMARY KEY NOT NULL,
    title TEXT NOT NULL,
    artist_name TEXT,
    duration INTEGER,
    url TEXT,
    image_url TEXT,
    album_id TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

### Albums Table
```sql
CREATE TABLE albums (
    id TEXT PRIMARY KEY NOT NULL,
    title TEXT NOT NULL,
    artist_name TEXT,
    release_date TEXT,
    image_url TEXT,
    track_count INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

### Artists Table
```sql
CREATE TABLE artists (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL UNIQUE,
    bio TEXT,
    image_url TEXT,
    genre TEXT,
    followers_count INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

## 🧪 Testing

```kotlin
@Test
fun `should return cached data when API fails`() = runTest {
    // Arrange
    val cachedSongs = listOf(Song("1", "Cached Song"))
    whenever(songDao.getAllSongs()).thenReturn(flowOf(cachedSongs.map { it.toEntity() }))
    whenever(apiInterface.getMusic()).thenThrow(IOException())
    
    // Act
    val result = musicRepository.syncSongsFromApi()
    
    // Assert
    assertEquals(cachedSongs, result)
}
```

## ⚙️ Configuration

### ProGuard Rules
```proguard
# Keep Room classes
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

# Keep database entities
-keep class com.delivery.database.entity.** { *; }
```

## 🚀 Migration Strategy

Để thêm migration trong tương lai:

```kotlin
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE songs ADD COLUMN new_column TEXT")
    }
}
```

## 📱 Best Practices

1. **Always use Flow** cho reactive data
2. **Implement proper error handling** với try-catch
3. **Use transactions** cho multiple operations
4. **Implement cache expiration** để tránh stale data
5. **Use indices** cho performance optimization
6. **Test repository layer** thoroughly

---

Module này cung cấp foundation mạnh mẽ cho offline-first architecture với Room database integration.
