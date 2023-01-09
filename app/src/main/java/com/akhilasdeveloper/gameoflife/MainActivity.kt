package com.akhilasdeveloper.gameoflife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.akhilasdeveloper.gameoflife.databinding.ActivityMainBinding
import com.akhilasdeveloper.span_grid_view.SpanGridView
import com.akhilasdeveloper.span_grid_view.models.Point
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var spanGridView:SpanGridView
    private val gameOfLife = GameOfLife()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spanGridView = binding.spanGridView

        gameOfLife.setListener(onSetBit = {
            spanGridView.plotPoint(it, ContextCompat.getColor(this,R.color.cell_color))
        }, onClearBit = {
            spanGridView.clearPoint(it)
        })

        spanGridView.setOnTouchListener(object: View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        gameOfLife.pause()
                    }
                    MotionEvent.ACTION_MOVE -> {
                    }
                    MotionEvent.ACTION_UP -> {
                        gameOfLife.resume()
                    }
                }
                return false
            }

        })

        spanGridView.setGridSelectListener(eventListener = object :
            SpanGridView.OnGridSelectListener {

            override fun onDraw(px: Point) {
                gameOfLife.setBit(px)
            }

            override fun onModeChange(mode: Int) {

            }
        })

        binding.play.setOnClickListener {
            gameOfLife.playPause()
        }

    }


}