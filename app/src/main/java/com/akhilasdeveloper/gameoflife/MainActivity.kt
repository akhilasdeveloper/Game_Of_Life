package com.akhilasdeveloper.gameoflife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.akhilasdeveloper.gameoflife.databinding.ActivityMainBinding
import com.akhilasdeveloper.span_grid_view.SpanGridView
import com.akhilasdeveloper.span_grid_view.models.Point
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var spanGridView:SpanGridView
    private val gameOfLife = GameOfLife()
    private lateinit var bottomSheetSettingsBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetMessagedBehavior: BottomSheetBehavior<LinearLayout>
    private var isDraw = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spanGridView = binding.spanGridView
        bottomSheetSettingsBehavior = BottomSheetBehavior.from(binding.bottomSheetSettings)
        bottomSheetMessagedBehavior = BottomSheetBehavior.from(binding.bottomSheetMessage)

        bottomSheetMessagedBehavior.toggleSheet()

        gameOfLife.setListener(onSetBit = {
            spanGridView.plotPoint(it, ContextCompat.getColor(this,R.color.cell_color))
        }, onClearBit = {
            spanGridView.clearPoint(it)
        }, onPlay = {
            binding.bottomAppBar.menu.findItem(R.id.play_stop).icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_round_stop_24,this.theme)
        }, onPause = {
            binding.bottomAppBar.menu.findItem(R.id.play_stop).icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_round_play_arrow_24,this.theme)
        }, statics = {
            binding.message.text = "Generation: ${it.generation}\nAlive: ${it.alive}"
        })

        binding.bottomAppBar.setNavigationOnClickListener {
            bottomSheetSettingsBehavior.toggleSheet()
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.draw_erase -> {
                    isDraw = !isDraw
                    if (isDraw){
                        menuItem.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_eraser_solid,this.theme)
                    }else{
                        menuItem.icon = ResourcesCompat.getDrawable(resources,R.drawable.ic_baseline_edit_24,this.theme)
                    }
                    true
                }
                R.id.play_stop -> {
                    gameOfLife.playPause()
                    true
                }
                R.id.info -> {
                    bottomSheetMessagedBehavior.toggleSheet()
                    true
                }
                else -> false
            }
        }

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
                if (isDraw){
                    gameOfLife.setBit(px)
                }else{
                    gameOfLife.clearBit(px)
                }
            }

            override fun onModeChange(mode: Int) {

            }
        })

        binding.gridEnabled.setOnClickListener {
            spanGridView.lineEnabled = binding.gridEnabled.isChecked
        }

        binding.nodeSlide.addOnChangeListener { _, value, _ ->
            spanGridView.brushSize = value.toInt()
        }

        binding.speedSlide.addOnChangeListener { _, value, _ ->
            gameOfLife.delay = value.toLong()
        }

        binding.closeSummary.setOnClickListener {
            bottomSheetMessagedBehavior.toggleSheet()
        }
    }

    private fun <V : View?> BottomSheetBehavior<V>.toggleSheet() {
        state = if (state == BottomSheetBehavior.STATE_EXPANDED)
            BottomSheetBehavior.STATE_HIDDEN
        else
            BottomSheetBehavior.STATE_EXPANDED
    }

}