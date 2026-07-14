package com.mcs1000h.remote.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mcs1000h.remote.ui.theme.CornerControl
import com.mcs1000h.remote.ui.theme.LocalAppPalette
import kotlin.math.abs
import kotlin.random.Random

private const val ROWS = 5
private const val COLS = 8
private const val PADDLE_WIDTH_FRACTION = 0.22f
private val PADDLE_HEIGHT = 14.dp
private val BALL_RADIUS = 7.dp
private val BRICK_GAP = 4.dp
private val BRICK_AREA_TOP = 12.dp
private val BRICK_AREA_HEIGHT = 160.dp
private val PADDLE_BOTTOM_MARGIN = 28.dp
private const val BASE_SPEED = 340f

private data class Brick(val row: Int, val col: Int, val alive: Boolean = true)

private enum class RunState { Playing, Won, Lost }

/**
 * Self-contained Breakout clone - the "tap the build number 3 times" easter egg. Drives its own
 * frame loop via [withFrameNanos] and draws everything on one [Canvas]; state lives in plain
 * Compose state so a hit (brick destroyed, life lost) just triggers a normal recomposition.
 */
@Composable
fun BreakoutGame(onExit: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    val brickColors = listOf(palette.danger, palette.warning, palette.accent, palette.success, palette.accent)
    val density = LocalDensity.current

    val paddleHeightPx = with(density) { PADDLE_HEIGHT.toPx() }
    val ballRadiusPx = with(density) { BALL_RADIUS.toPx() }
    val brickAreaTopPx = with(density) { BRICK_AREA_TOP.toPx() }
    val brickAreaHeightPx = with(density) { BRICK_AREA_HEIGHT.toPx() }
    val brickGapPx = with(density) { BRICK_GAP.toPx() }
    val paddleBottomMarginPx = with(density) { PADDLE_BOTTOM_MARGIN.toPx() }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var bricks by remember { mutableStateOf(buildBricks()) }
    var paddleX by remember { mutableFloatStateOf(0.5f) }
    var ballPos by remember { mutableStateOf(Offset.Zero) }
    var ballVel by remember { mutableStateOf(Offset(BASE_SPEED, -BASE_SPEED)) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var runState by remember { mutableStateOf(RunState.Playing) }
    var epoch by remember { mutableIntStateOf(0) }

    fun resetBall(width: Float, height: Float) {
        ballPos = Offset(width / 2f, height - paddleBottomMarginPx - paddleHeightPx - 60f)
        val dir = if (Random.nextBoolean()) 1f else -1f
        ballVel = Offset(BASE_SPEED * dir, -BASE_SPEED)
    }

    fun restart() {
        bricks = buildBricks()
        score = 0
        lives = 3
        runState = RunState.Playing
        paddleX = 0.5f
        epoch++
    }

    LaunchedEffect(canvasSize, epoch) {
        val w = canvasSize.width.toFloat()
        val h = canvasSize.height.toFloat()
        if (w <= 0f || h <= 0f) return@LaunchedEffect

        resetBall(w, h)
        var lastNanos = withFrameNanos { it }

        while (runState == RunState.Playing) {
            withFrameNanos { now ->
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceAtMost(0.032f)
                lastNanos = now

                var bx = ballPos.x
                var by = ballPos.y
                var vx = ballVel.x
                var vy = ballVel.y

                bx += vx * dt
                by += vy * dt

                if (bx - ballRadiusPx < 0f) {
                    bx = ballRadiusPx
                    vx = abs(vx)
                } else if (bx + ballRadiusPx > w) {
                    bx = w - ballRadiusPx
                    vx = -abs(vx)
                }
                if (by - ballRadiusPx < 0f) {
                    by = ballRadiusPx
                    vy = abs(vy)
                }

                // Paddle.
                val paddleWidthPx = PADDLE_WIDTH_FRACTION * w
                val paddleCenterX = paddleX * w
                val paddleLeft = paddleCenterX - paddleWidthPx / 2f
                val paddleRight = paddleCenterX + paddleWidthPx / 2f
                val paddleTop = h - paddleBottomMarginPx - paddleHeightPx
                if (vy > 0 &&
                    by + ballRadiusPx >= paddleTop &&
                    by - ballRadiusPx <= paddleTop + paddleHeightPx &&
                    bx >= paddleLeft && bx <= paddleRight
                ) {
                    by = paddleTop - ballRadiusPx
                    val hitFrac = ((bx - paddleLeft) / paddleWidthPx) - 0.5f
                    vx = hitFrac * BASE_SPEED * 2.4f
                    vy = -abs(vy) * 1.02f
                }

                // Bricks - simple circle-vs-AABB, first hit per frame wins.
                val brickW = (w - brickGapPx * (COLS + 1)) / COLS
                val brickH = (brickAreaHeightPx - brickGapPx * (ROWS + 1)) / ROWS
                var hitBrick: Brick? = null
                for (brick in bricks) {
                    if (!brick.alive) continue
                    val left = brickGapPx + brick.col * (brickW + brickGapPx)
                    val top = brickAreaTopPx + brickGapPx + brick.row * (brickH + brickGapPx)
                    val right = left + brickW
                    val bottom = top + brickH
                    val closestX = bx.coerceIn(left, right)
                    val closestY = by.coerceIn(top, bottom)
                    val dx = bx - closestX
                    val dy = by - closestY
                    if (dx * dx + dy * dy <= ballRadiusPx * ballRadiusPx) {
                        hitBrick = brick
                        vy = -vy
                        break
                    }
                }
                if (hitBrick != null) {
                    val hit = hitBrick
                    bricks = bricks.map { if (it.row == hit.row && it.col == hit.col) it.copy(alive = false) else it }
                    score += 10
                    if (bricks.none { it.alive }) {
                        runState = RunState.Won
                    }
                }

                if (by - ballRadiusPx > h) {
                    lives -= 1
                    if (lives <= 0) {
                        runState = RunState.Lost
                    } else {
                        resetBall(w, h)
                        bx = ballPos.x
                        by = ballPos.y
                        vx = ballVel.x
                        vy = ballVel.y
                    }
                }

                ballPos = Offset(bx, by)
                ballVel = Offset(vx, vy)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Breakout", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "Score $score  ·  Lives $lives",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .panelSurface(palette, CornerControl)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onExit)
                    .padding(10.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Exit game", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val ballColor = MaterialTheme.colorScheme.onSurface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .panelSurface(palette)
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val fx = change.position.x / size.width.toFloat()
                        paddleX = fx.coerceIn(PADDLE_WIDTH_FRACTION / 2f, 1f - PADDLE_WIDTH_FRACTION / 2f)
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBreakoutFrame(
                    bricks = bricks,
                    brickColors = brickColors,
                    brickAreaTopPx = brickAreaTopPx,
                    brickAreaHeightPx = brickAreaHeightPx,
                    brickGapPx = brickGapPx,
                    paddleX = paddleX,
                    paddleWidthFraction = PADDLE_WIDTH_FRACTION,
                    paddleHeightPx = paddleHeightPx,
                    paddleBottomMarginPx = paddleBottomMarginPx,
                    ballPos = ballPos,
                    ballRadiusPx = ballRadiusPx,
                    accent = palette.accent,
                    ballColor = ballColor,
                )
            }

            if (runState != RunState.Playing) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .panelSurface(palette)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (runState == RunState.Won) "You win" else "Game over",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Score $score",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SolidButton(text = "Play again", onClick = { restart() })
                }
            }
        }
    }
}

private fun buildBricks(): List<Brick> =
    (0 until ROWS).flatMap { r -> (0 until COLS).map { c -> Brick(r, c) } }

private fun DrawScope.drawBreakoutFrame(
    bricks: List<Brick>,
    brickColors: List<Color>,
    brickAreaTopPx: Float,
    brickAreaHeightPx: Float,
    brickGapPx: Float,
    paddleX: Float,
    paddleWidthFraction: Float,
    paddleHeightPx: Float,
    paddleBottomMarginPx: Float,
    ballPos: Offset,
    ballRadiusPx: Float,
    accent: Color,
    ballColor: Color,
) {
    val w = size.width
    val h = size.height
    val brickW = (w - brickGapPx * (COLS + 1)) / COLS
    val brickH = (brickAreaHeightPx - brickGapPx * (ROWS + 1)) / ROWS

    for (brick in bricks) {
        if (!brick.alive) continue
        val left = brickGapPx + brick.col * (brickW + brickGapPx)
        val top = brickAreaTopPx + brickGapPx + brick.row * (brickH + brickGapPx)
        drawRect(
            color = brickColors[brick.row % brickColors.size],
            topLeft = Offset(left, top),
            size = Size(brickW, brickH),
        )
    }

    val paddleWidthPx = paddleWidthFraction * w
    val paddleLeft = paddleX * w - paddleWidthPx / 2f
    val paddleTop = h - paddleBottomMarginPx - paddleHeightPx
    drawRect(
        color = accent,
        topLeft = Offset(paddleLeft, paddleTop),
        size = Size(paddleWidthPx, paddleHeightPx),
    )

    drawCircle(color = ballColor, radius = ballRadiusPx, center = ballPos)
}
