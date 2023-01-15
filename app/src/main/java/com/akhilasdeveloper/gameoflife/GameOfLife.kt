package com.akhilasdeveloper.gameoflife

import com.akhilasdeveloper.span_grid_view.models.Point
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class GameOfLife {
    private val tempGen = ConcurrentHashMap<Point, Int>()
    private var gridHash = ConcurrentHashMap<Point, Int>()
    private var job: Job? = null

    private var statics: ((Statics) -> Unit)? = null
    private var onPlay: (() -> Unit)? = null
    private var onPause: (() -> Unit)? = null
    private var onSetBit: ((Point) -> Unit)? = null
    private var onSetBits: ((ArrayList<Point>) -> Unit)? = null
    private var onClearBit: ((Point) -> Unit)? = null
    private var onClearBits: ((ArrayList<Point>) -> Unit)? = null
    private var isPlaying = false
    private var isPaused = true
    private var generation = 0L
    var delay = 0L
    private var generateRules = arrayListOf<Int>()
    private var surviveRules = arrayListOf<Int>()

    init {
        generateInfo()
    }

    fun setListener(
        onSetBit: (Point) -> Unit,
        onSetBits: (ArrayList<Point>) -> Unit,
        onClearBit: (Point) -> Unit,
        onClearBits: (ArrayList<Point>) -> Unit,
        onPlay: () -> Unit,
        onPause: () -> Unit,
        statics: (Statics) -> Unit
    ) {
        this.onSetBits = onSetBits
        this.onSetBit = onSetBit
        this.onClearBit = onClearBit
        this.onClearBits = onClearBits
        this.onPlay = onPlay
        this.onPause = onPause
        this.statics = statics
    }

    fun playPause() {
        isPlaying = !isPlaying

        if (isPlaying && !isPaused) {
            onPlay?.invoke()
        } else {
            onPause?.invoke()
        }
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

    private fun generateInfo() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                statics?.invoke(
                    Statics(
                        generation = generation,
                        alive = gridHash.filter { it.value and 1 == 1 }.size.toLong()
                    )
                )
                delay(1)
            }
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
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

    private fun isSet(px: Point): Boolean = ((gridHash[px]?:0) and 1) == 1

    fun setBit(px: Point) {
        if (!isSet(px)) {
            val data: Int = gridHash[px] ?: 0
            gridHash[px] = data or 1         //set first bit as 0
            onSetBit?.invoke(px)

            getNeighbours(px).forEach {
                var dat = gridHash[it] ?: 0
                dat += 2
                gridHash[it] = dat
                if ((gridHash[it] ?: 0) == 0)
                    gridHash.remove(it)
            }
        }
    }

    private fun setBits(pxs: ArrayList<Point>) {
        val setList = arrayListOf<Point>()
        pxs.forEach { px->
            if (!isSet(px)) {
                val data: Int = gridHash[px] ?: 0
                gridHash[px] = data or 1         //set first bit as 0
                setList.add(px)

                getNeighbours(px).forEach {
                    var dat = gridHash[it] ?: 0
                    dat += 2
                    gridHash[it] = dat
                    if ((gridHash[it] ?: 0) == 0)
                        gridHash.remove(it)
                }
            }
        }
        onSetBits?.invoke(setList)
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
                if ((gridHash[it] ?: 0) == 0)
                    gridHash.remove(it)
            }
        }
    }

    private fun clearBits(pxs: ArrayList<Point>) {
        val clearList = arrayListOf<Point>()
        pxs.forEach { px ->
            if (isSet(px)) {
                gridHash[px]?.let {
                    gridHash[px] = it and 1.inv()
                    clearList.add(px)
                }  //set first bit as 0

                getNeighbours(px).forEach {
                    var dat = gridHash[it] ?: 0
                    dat -= 2
                    gridHash[it] = dat
                    if ((gridHash[it] ?: 0) == 0)
                        gridHash.remove(it)
                }
            }
        }
        onClearBits?.invoke(clearList)
    }

    fun setGenerateRules(generateRules: ArrayList<Int>){
        this.generateRules.clear()
        this.generateRules.addAll(generateRules)
    }

    fun setSurviveRules(surviveRules: ArrayList<Int>){
        this.surviveRules.clear()
        this.surviveRules.addAll(surviveRules)
    }

    fun calculateNextGen() {

        try {
            val setList = arrayListOf<Point>()
            val clearList = arrayListOf<Point>()

            tempGen.putAll(gridHash)
            tempGen.forEach {
                val neighbours = it.value shr 1
                if (it.value and 1 == 1) {
                    if (!surviveRules.contains(neighbours)) {
                        clearList.add(it.key)
                    }
                } else {
                    if (generateRules.contains(neighbours)) {
                        setList.add(it.key)
                    }
                }
            }

            clearBits(clearList)
            setBits(setList)
            generation++
        } catch (e: Exception) {
            Timber.e("$e")
        }

    }
}