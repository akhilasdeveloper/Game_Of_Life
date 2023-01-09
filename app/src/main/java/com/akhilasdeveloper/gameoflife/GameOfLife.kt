package com.akhilasdeveloper.gameoflife

import com.akhilasdeveloper.span_grid_view.models.Point
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class GameOfLife {
    private val tempGen = ConcurrentHashMap<Point, Int>()
    private var gridHash: ConcurrentHashMap<Point, Int> = ConcurrentHashMap<Point, Int>()
    private var job: Job? = null

    private var onSetBit: ((Point) -> Unit)? = null
    private var onClearBit: ((Point) -> Unit)? = null
    private var isPlaying = false
    private var isPaused = true
    var delay = 0L

    fun setListener(onSetBit: (Point) -> Unit, onClearBit: (Point) -> Unit) {
        this.onSetBit = onSetBit
        this.onClearBit = onClearBit
    }

    fun playPause() {
        isPlaying = !isPlaying
        if (job == null) {
            job = CoroutineScope(Dispatchers.Default).launch {
                while (true) {
                    if (isPlaying && !isPaused) {
                        calculateNextGen()
                        delay(delay)
                    }
                }
            }
        }
    }

    fun pause(){
        isPaused = true
    }

    fun resume(){
        isPaused = false
    }

    private fun getNeighbours(px: Point) = arrayOf(
        Point(px.x - 1, px.y - 1),
        Point(px.x, px.y - 1),
        Point(px.x + 1, px.y - 1),
        Point(px.x + 1, px.y),
        Point(px.x + 1, px.y + 1),
        Point(px.x, px.y + 1),
        Point(px.x - 1, px.y + 1),
        Point(px.x - 1, px.y),
    )

    private fun isSet(px: Point): Boolean {
        gridHash[px]?.let {
            return (it and 1) == 1
        }
        return false
    }

    fun setBit(px: Point) {
        if (!isSet(px)) {
            val data: Int = gridHash[px] ?: 0
            gridHash[px] = data or 1         //set first bit as 0
            onSetBit?.invoke(px)

            getNeighbours(px).forEach {
                var dat = gridHash[it] ?: 0
                dat += 2
                gridHash[it] = dat
                if (gridHash[it] ?: 0 == 0)
                    gridHash.remove(it)
            }
        }
    }

    fun clearBit(px: Point) {
        if (isSet(px)) {
            gridHash[px]?.let {
                gridHash[px] = it and 1.inv()
                onClearBit?.invoke(px)
            }  //set first bit as 0

            getNeighbours(px).forEach {
                var dat = gridHash[it] ?: 0
                dat -= 2
                gridHash[it] = dat
                if (gridHash[it] ?: 0 == 0)
                    gridHash.remove(it)
            }
        }
    }

    private fun calculateNextGen() {

        try {
            tempGen.putAll(gridHash)
            tempGen.forEach {
                val neighbours = it.value shr 1
                if (it.value and 1 == 1) {
                    if ((neighbours != 2) && (neighbours != 3)) {
                        clearBit(it.key)
                    }
                } else {
                    if (neighbours == 3) {
                        setBit(it.key)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("$e")
        }

    }
}