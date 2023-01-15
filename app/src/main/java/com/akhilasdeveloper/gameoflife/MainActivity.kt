package com.akhilasdeveloper.gameoflife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import com.akhilasdeveloper.gameoflife.databinding.ActivityMainBinding
import com.akhilasdeveloper.span_grid_view.SpanGridView
import com.akhilasdeveloper.span_grid_view.models.Point
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var spanGridView: SpanGridView
    private val gameOfLife = GameOfLife()
    private lateinit var bottomSheetSettingsBehavior: BottomSheetBehavior<NestedScrollView>
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
            spanGridView.plotPoint(it, ContextCompat.getColor(this, R.color.cell_color))
        }, onClearBit = {
            spanGridView.clearPoint(it)
        }, onPlay = {
            binding.bottomAppBar.menu.findItem(R.id.play_stop).icon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_round_stop_24, this.theme)
            binding.bottomAppBar.menu.findItem(R.id.next_step).isVisible = false
        }, onPause = {
            binding.bottomAppBar.menu.findItem(R.id.play_stop).icon = ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_round_play_arrow_24,
                this.theme
            )
            binding.bottomAppBar.menu.findItem(R.id.next_step).isVisible = true
        }, statics = {
            binding.message.text = "Generation: ${it.generation}\nAlive: ${it.alive}"
        }, onSetBits = {
            spanGridView.plotPoints(it, ContextCompat.getColor(this, R.color.cell_color))
        }, onClearBits = {
            spanGridView.clearPoints(it)
        })

        binding.bottomAppBar.setNavigationOnClickListener {
            bottomSheetSettingsBehavior.toggleSheet()
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.draw_erase -> {
                    isDraw = !isDraw
                    if (isDraw) {
                        menuItem.icon = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_eraser_solid,
                            this.theme
                        )
                    } else {
                        menuItem.icon = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_baseline_edit_24,
                            this.theme
                        )
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
                R.id.next_step -> {
                    gameOfLife.calculateNextGen()
                    true
                }
                else -> false
            }

        }

        spanGridView.setOnTouchListener(object : View.OnTouchListener {
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
                if (isDraw) {
                    gameOfLife.setBit(px)
                } else {
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

        binding.resetRule.setOnClickListener {
            setDefaultRule()
        }

        val clickListener = View.OnClickListener {
            setRules()
        }

        binding.gen1.setOnClickListener(clickListener)
        binding.gen2.setOnClickListener(clickListener)
        binding.gen3.setOnClickListener(clickListener)
        binding.gen4.setOnClickListener(clickListener)
        binding.gen5.setOnClickListener(clickListener)
        binding.gen6.setOnClickListener(clickListener)
        binding.gen7.setOnClickListener(clickListener)
        binding.gen8.setOnClickListener(clickListener)

        binding.sur1.setOnClickListener(clickListener)
        binding.sur2.setOnClickListener(clickListener)
        binding.sur3.setOnClickListener(clickListener)
        binding.sur4.setOnClickListener(clickListener)
        binding.sur5.setOnClickListener(clickListener)
        binding.sur6.setOnClickListener(clickListener)
        binding.sur7.setOnClickListener(clickListener)
        binding.sur8.setOnClickListener(clickListener)

        setDefaultRule()
    }

    private fun setDefaultRule() {
        binding.apply {
            sur1.isChecked = false
            sur2.isChecked = true
            sur3.isChecked = true
            sur4.isChecked = false
            sur5.isChecked = false
            sur6.isChecked = false
            sur7.isChecked = false
            sur8.isChecked = false

            gen1.isChecked = false
            gen2.isChecked = false
            gen3.isChecked = true
            gen4.isChecked = false
            gen5.isChecked = false
            gen6.isChecked = false
            gen7.isChecked = false
            gen8.isChecked = false
        }

        setRules()
    }

    private fun setRules() {
        val genRules = arrayListOf<Int>()
        val surRules = arrayListOf<Int>()
        binding.apply {
            if (sur1.isChecked) surRules.add(1)
            if (sur2.isChecked) surRules.add(2)
            if (sur3.isChecked) surRules.add(3)
            if (sur4.isChecked) surRules.add(4)
            if (sur5.isChecked) surRules.add(5)
            if (sur6.isChecked) surRules.add(6)
            if (sur7.isChecked) surRules.add(7)
            if (sur8.isChecked) surRules.add(8)

            if (gen1.isChecked) genRules.add(1)
            if (gen2.isChecked) genRules.add(2)
            if (gen3.isChecked) genRules.add(3)
            if (gen4.isChecked) genRules.add(4)
            if (gen5.isChecked) genRules.add(5)
            if (gen6.isChecked) genRules.add(6)
            if (gen7.isChecked) genRules.add(7)
            if (gen8.isChecked) genRules.add(8)
        }

        gameOfLife.setGenerateRules(genRules)
        gameOfLife.setSurviveRules(surRules)
    }

    private fun <V : View?> BottomSheetBehavior<V>.toggleSheet() {
        state = if (state == BottomSheetBehavior.STATE_EXPANDED)
            BottomSheetBehavior.STATE_HIDDEN
        else
            BottomSheetBehavior.STATE_EXPANDED
    }

}