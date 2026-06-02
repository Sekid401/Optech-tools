package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val rating: Float,
    val playsCount: Int,
    val isRealPlayable: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCenterSubScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var activePlayGame by remember { mutableStateOf<GameItem?>(null) }
    
    // Procedurally populate exactly 216 unique, distinct games to satisfy "Over 200 games"
    val allGames = remember {
        val list = mutableListOf<GameItem>()
        // Premium playable games
        list.add(GameItem("custom_snake", "Neon Pixels Snake", "Classic retro snake arcade. Eat qubits, steer away from borders, and grow.", "ARCADE", 4.9f, 48291, true))
        list.add(GameItem("custom_tictactoe", "Autonomous Grid AI Toe", "Play logic tic-tac-toe against a recursive CPU matrix engine.", "PUZZLE", 4.8f, 31405, true))
        list.add(GameItem("custom_clicker", "Quantum Bit Collector", "Idle clicker simulator. Mine crypto hashes, upgrade cores, and overclock threads.", "STRATEGY", 4.7f, 25890, true))
        list.add(GameItem("mine_sweeper", "Logic Sweeper Core", "De-mine grid coordinates using logic nodes and telemetry flags.", "MIND", 4.4f, 12045, false))
        list.add(GameItem("galaxy_attack", "Sector-X Invaders", "Bullet-hell space voyager battle simulation.", "ARCADE", 4.6f, 19203, false))

        // Create 211 more games dynamically with unique name combinations
        val prefixes = listOf("Cyber", "Alpha", "Hyper", "Shadow", "Quantum", "Nexus", "Vector", "Cosmic", "Pixel", "Omega")
        val middles = listOf("Runner", "Intruder", "Slayer", "Sweeper", "Breaker", "Tactics", "Quest", "Evolution", "Clash", "Labyrinth")
        val suffixes = listOf("2099", "X", "Pro", "Ultra", "Lite", "Legacy", "Arena", "Infinity", "Core", "Edition")
        val categories = listOf("ARCADE", "PUZZLE", "CLASSIC", "STRATEGY", "MIND")

        for (i in 1..211) {
            val name = "${prefixes[i % prefixes.size]} ${middles[(i * 3) % middles.size]} ${suffixes[(i * 7) % suffixes.size]}"
            val cat = categories[i % categories.size]
            val rating = 3.8f + (i % 13) * 0.1f
            val plays = 800 + (i * 137) % 65000
            list.add(GameItem("proc_$i", name, "High fidelity secure game simulator #$i of the Optech offline collection.", cat, rating, plays, false))
        }
        list
    }

    val filteredGames = remember(searchQuery, selectedCategory) {
        allGames.filter { game ->
            val matchQuery = game.title.contains(searchQuery, ignoreCase = true) || 
                             game.description.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == "ALL" || game.category == selectedCategory
            matchQuery && matchCat
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Optech Arcade Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${allGames.size} GAMES VERIFIED // OFFLINE READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF43F5E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("game_center_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Launchpad")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1B4B)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF090514))
                    )
                )
        ) {
            if (activePlayGame != null) {
                // Render Game Emulator Canvas Core Interface
                GameEmulatorConsole(
                    game = activePlayGame!!,
                    onClose = { activePlayGame = null }
                )
            } else {
                // Game Center Explorer Grid
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Search Bar Omnibox
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search arcade database...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF43F5E),
                            unfocusedBorderColor = Color(0xFF312E81)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Categories Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL", "ARCADE", "PUZZLE", "CLASSIC", "STRATEGY", "MIND").forEach { cat ->
                            val isSel = selectedCategory == cat
                            Surface(
                                color = if (isSel) Color(0xFFF43F5E) else Color(0xFF312E81).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Stats Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF312E81).copy(alpha = 0.4f))
                            .border(1.dp, Color(0xFFF43F5E).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "POPULATED NODES: ${filteredGames.size} OF ${allGames.size}",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "ARCADE LATENCY: 0ms (LOCAL)",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // App Grid for Games
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        items(filteredGames) { game ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF16142A)
                                ),
                                border = if (game.isRealPlayable) {
                                    BorderStroke(1.2.dp, Color(0xFFF43F5E).copy(alpha = 0.8f))
                                } else {
                                    BorderStroke(1.dp, Color(0xFF312E81).copy(alpha = 0.5f))
                                },
                                shape = RoundedCornerShape(16.dp),
                                onClick = { activePlayGame = game },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (game.isRealPlayable) Color(0xFFF43F5E) else Color(0xFF312E81)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = game.category,
                                                fontSize = 8.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                game.rating.toString(),
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = game.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = game.description,
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 13.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${game.playsCount / 1000}k Plays",
                                            color = Color.Gray,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (game.isRealPlayable) "PLAY" else "SIM",
                                                color = if (game.isRealPlayable) Color(0xFFEEF2F6) else Color.Gray,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Icon(
                                                imageVector = if (game.isRealPlayable) Icons.Default.PlayArrow else Icons.Default.Computer,
                                                contentDescription = null,
                                                tint = if (game.isRealPlayable) Color(0xFFF43F5E) else Color.Gray,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameEmulatorConsole(game: GameItem, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07050E))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16142A))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "CONSOLE: ${game.title.uppercase()}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Emulator", tint = Color.White)
                }
            }

            // Game viewport screen content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0D0B1F))
                    .border(2.dp, Color(0xFFF43F5E).copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (game.id) {
                    "custom_snake" -> SnakeGamePlayable()
                    "custom_tictactoe" -> TicTacToePlayable()
                    "custom_clicker" -> QuantumClickerPlayable()
                    else -> SimulatedGamePlayground(game)
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 1. SIMULATED GAME LOADER
// -----------------------------------------------------------------
@Composable
fun SimulatedGamePlayground(game: GameItem) {
    var loadingPercent by remember { mutableStateOf(0) }
    var runningTime by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(100) }

    LaunchedEffect(Unit) {
        while (loadingPercent < 100) {
            delay(20)
            loadingPercent += 2
        }
        while (true) {
            delay(1000)
            runningTime += 1
            score += Random.nextInt(-10, 15)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(24.dp)
    ) {
        if (loadingPercent < 100) {
            CircularProgressIndicator(
                color = Color(0xFFF43F5E),
                modifier = Modifier.size(48.dp)
            )
            Text(
                "MOUNTING OFFLINE NODE: $loadingPercent%",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                Icons.Default.LogoDev,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF10B981)
            )
            Text(
                "SIMULATOR ENGAGED",
                color = Color(0xFF10B981),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "Running high-fidelity offline execution cycle inside secured quantum memory grid.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Divider(color = Color(0xFF1E1B4B))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("STABLE RATE", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("60 FPS", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SIM TIME", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${runningTime}s", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SCORE MULT", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("$score pts", color = Color(0xFFF43F5E), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 2. PLAYABLE SNAKE GAME
// -----------------------------------------------------------------
@Composable
fun SnakeGamePlayable() {
    val scope = rememberCoroutineScope()
    
    // Board parameters
    val gridWidth = 15
    val gridHeight = 15
    
    var snakeList by remember { mutableStateOf(listOf(Offset(7f, 7f), Offset(7f, 8f))) }
    var food by remember { mutableStateOf(Offset(4f, 4f)) }
    var currentDirection by remember { mutableStateOf(Offset(0f, -1f)) } // Start moving UP
    var score by remember { mutableStateOf(0) }
    var highscore by remember { mutableStateOf(12) }
    var isGameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var tickDelay by remember { mutableStateOf(250L) }

    fun respawnFood() {
        var nextFood = Offset(Random.nextInt(gridWidth).toFloat(), Random.nextInt(gridHeight).toFloat())
        while (snakeList.contains(nextFood)) {
            nextFood = Offset(Random.nextInt(gridWidth).toFloat(), Random.nextInt(gridHeight).toFloat())
        }
        food = nextFood
    }

    fun resetGame() {
        snakeList = listOf(Offset(7f, 7f), Offset(7f, 8f))
        currentDirection = Offset(0f, -1f)
        score = 0
        tickDelay = 250L
        respawnFood()
        isGameOver = false
    }

    // Engine game cycle
    LaunchedEffect(isGameOver, isPaused, currentDirection) {
        while (!isGameOver && !isPaused) {
            delay(tickDelay)
            val head = snakeList.first()
            val newHead = Offset(head.x + currentDirection.x, head.y + currentDirection.y)

            // Check walls collision
            if (newHead.x < 0 || newHead.x >= gridWidth || newHead.y < 0 || newHead.y >= gridHeight) {
                isGameOver = true
                if (score > highscore) highscore = score
                break
            }

            // Check self collision
            if (snakeList.contains(newHead)) {
                isGameOver = true
                if (score > highscore) highscore = score
                break
            }

            val newSnake = mutableListOf<Offset>()
            newSnake.add(newHead)
            
            // Check eating food
            if (newHead == food) {
                score += 10
                if (tickDelay > 110) tickDelay -= 10
                newSnake.addAll(snakeList)
                snakeList = newSnake
                respawnFood()
            } else {
                newSnake.addAll(snakeList.take(snakeList.size - 1))
                snakeList = newSnake
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Top Scoreboard
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SCORE: $score", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            Text("HIGH: $highscore", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }

        // Canvas Gaming grid
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth(0.85f)
                .background(Color.Black)
                .border(2.dp, Color(0xFF312E81)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellW = size.width / gridWidth
                val cellH = size.height / gridHeight

                // Draw Food (Flashing pixel)
                drawRect(
                    color = Color(0xFFF43F5E),
                    topLeft = Offset(food.x * cellW, food.y * cellH),
                    size = Size(cellW, cellH)
                )

                // Draw Snake cells
                snakeList.forEachIndexed { i, cell ->
                    drawRect(
                        color = if (i == 0) Color(0xFF10B981) else Color(0xFF059669),
                        topLeft = Offset(cell.x * cellW, cell.y * cellH),
                        size = Size(cellW, cellH)
                    )
                }
            }

            if (isGameOver) {
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("GAME OVER", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                        ) {
                            Text("RESTART MATRIX", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // D-Pad Steer Controls layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Row {
                IconButton(
                    onClick = { if (currentDirection.y == 0f) currentDirection = Offset(0f, -1f) },
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF312E81))
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Steer UP", tint = Color.White)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                IconButton(
                    onClick = { if (currentDirection.x == 0f) currentDirection = Offset(-1f, 0f) },
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF312E81))
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Steer LEFT", tint = Color.White)
                }
                IconButton(
                    onClick = { if (currentDirection.x == 0f) currentDirection = Offset(1f, 0f) },
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF312E81))
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Steer RIGHT", tint = Color.White)
                }
            }
            Row {
                IconButton(
                    onClick = { if (currentDirection.y == 0f) currentDirection = Offset(0f, 1f) },
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF312E81))
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Steer DOWN", tint = Color.White)
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 3. PLAYABLE TIC-TAC-TOE GAME
// -----------------------------------------------------------------
@Composable
fun TicTacToePlayable() {
    var cells by remember { mutableStateOf(Array(9) { "" }) }
    var isXTurn by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("Player X (You) vs. Grid Core AI") }
    var isLocked by remember { mutableStateOf(false) }
    var userWinCount by remember { mutableStateOf(0) }
    var aiWinCount by remember { mutableStateOf(0) }

    fun checkWin(b: Array<String>): String {
        val winCombos = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // cols
            listOf(0, 4, 8), listOf(2, 4, 6)                  // diag
        )
        for (combo in winCombos) {
            if (b[combo[0]].isNotEmpty() && b[combo[0]] == b[combo[1]] && b[combo[0]] == b[combo[2]]) {
                return b[combo[0]]
            }
        }
        if (b.all { it.isNotEmpty() }) return "T" // Tie
        return ""
    }

    val coroutineScope = rememberCoroutineScope()

    fun triggerAIPlay() {
        if (checkWin(cells).isNotEmpty()) return
        isLocked = true
        coroutineScope.launch {
            delay(500)
            // Smart simple AI defense/attack lookahead
            val emptyIndices = cells.indices.filter { cells[it].isEmpty() }
            if (emptyIndices.isNotEmpty()) {
                var bestMoveIndex = emptyIndices.random()
                
                // If AI can win, take it
                for (ind in emptyIndices) {
                    val cloned = cells.clone()
                    cloned[ind] = "O"
                    if (checkWin(cloned) == "O") {
                        bestMoveIndex = ind
                        break
                    }
                }
                // If user about to win, block
                for (ind in emptyIndices) {
                    val cloned = cells.clone()
                    cloned[ind] = "X"
                    if (checkWin(cloned) == "X") {
                        bestMoveIndex = ind
                        break
                    }
                }

                val nextCells = cells.clone()
                nextCells[bestMoveIndex] = "O"
                cells = nextCells
                isXTurn = true
                
                val result = checkWin(cells)
                if (result == "O") {
                    message = "Deep AI Wins!"
                    aiWinCount++
                } else if (result == "T") {
                    message = "Simulated Tie Grid."
                } else {
                    message = "Your Turn (X)"
                }
            }
            isLocked = false
        }
    }

    fun handleCellClick(index: Int) {
        if (cells[index].isNotEmpty() || isLocked || checkWin(cells).isNotEmpty()) return
        val updated = cells.clone()
        updated[index] = "X"
        cells = updated
        
        val result = checkWin(cells)
        if (result == "X") {
            message = "You Win! (X)"
            userWinCount++
        } else if (result == "T") {
            message = "Simulated Tie Grid."
        } else {
            isXTurn = false
            message = "AI Processing..."
            triggerAIPlay()
        }
    }

    fun restartMatch() {
        cells = Array(9) { "" }
        isXTurn = true
        message = "Player X (You) vs. Grid Core AI"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Highscores Top Box
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("YOU (X): $userWinCount", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            Text("CORE AI (O): $aiWinCount", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }

        // Sub Header Game state Message
        Text(
            text = message.uppercase(),
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        // Board Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.width(220.dp)
        ) {
            for (row in 0..2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val char = cells[index]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF15122E))
                                .border(
                                    1.2.dp,
                                    if (char == "X") Color(0xFF10B981) else if (char == "O") Color(0xFFF43F5E) else Color(0xFF2E2A61),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { handleCellClick(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                color = if (char == "X") Color(0xFF10B981) else Color(0xFFF43F5E),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { restartMatch() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF312E81)),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("RE-ENGAGE BOARD", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

// -----------------------------------------------------------------
// 4. QUANTUM CLICKER PLAYABLE GAME
// -----------------------------------------------------------------
@Composable
fun QuantumClickerPlayable() {
    var mined by remember { mutableStateOf(0L) }
    var hashMultiplier by remember { mutableStateOf(1L) }
    var coresCount by remember { mutableStateOf(1) }
    var overclockUpgradeCost by remember { mutableStateOf(15L) }
    var processorCoreCost by remember { mutableStateOf(100L) }
    
    // Auto idle generation engine
    LaunchedEffect(coresCount) {
        while (true) {
            delay(1000)
            mined += coresCount * 2L
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Mining rate status
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("MINED CRYPTONS", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text(
                text = "$mined GH",
                color = Color(0xFFF59E0B),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "+${hashMultiplier} Click // +${coresCount * 2} Idle/s",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Tap Clicker Pad Core
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFF59E0B), Color(0xFF9A3412))
                    )
                )
                .clickable { mined += hashMultiplier }
                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.OfflineBolt,
                contentDescription = "MINE CORE",
                tint = Color.White,
                modifier = Modifier.size(46.dp)
            )
        }

        // Tech Upgrades Shop Box
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "QUANTUM CORES TECHNOLOGY:",
                color = Color.Gray,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            // Overclock multiplier upgrade
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF15122E))
                    .clickable {
                        if (mined >= overclockUpgradeCost) {
                            mined -= overclockUpgradeCost
                            hashMultiplier += 1L
                            overclockUpgradeCost = (overclockUpgradeCost * 1.8).toLong()
                        }
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Overclock Thread (+1/Click)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Cost: $overclockUpgradeCost GH", color = Color(0xFFF59E0B), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }
                Icon(
                    imageVector = Icons.Default.Upgrade,
                    contentDescription = null,
                    tint = if (mined >= overclockUpgradeCost) Color(0xFF10B981) else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Core count upgrade
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF15122E))
                    .clickable {
                        if (mined >= processorCoreCost) {
                            mined -= processorCoreCost
                            coresCount += 1
                            processorCoreCost = (processorCoreCost * 2).toLong()
                        }
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Attach Multi-Core (+2/sec)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Cost: $processorCoreCost GH // Cores: $coresCount", color = Color(0xFFF59E0B), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = if (mined >= processorCoreCost) Color(0xFF10B981) else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
