package com.bluehub.fastmixer.common.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.screens.mixing.AudioFileUiState
import com.bluehub.fastmixer.screens.mixing.FileWaveViewStore
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.ceil


@BindingMethods(value = [
    BindingMethod(type = FileWaveView::class, attribute = "samplesReader", method = "setSamplesReader"),
    BindingMethod(type = FileWaveView::class, attribute = "audioFileUiState", method = "setAudioFileUiState"),
    BindingMethod(type = FileWaveView::class, attribute = "fileWaveViewStore", method = "setFileWaveViewStore")
])
class FileWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {
    private val mAudioFileUiState: BehaviorSubject<AudioFileUiState> = BehaviorSubject.create()
    var mSamplesReader: BehaviorSubject<Function<Int, Deferred<Array<Float>>>> = BehaviorSubject.create()
    private val mFileWaveViewStore: BehaviorSubject<FileWaveViewStore> = BehaviorSubject.create()

    private lateinit var mAudioWidgetSlider: AudioWidgetSlider

    var mRawPoints: BehaviorSubject<Array<Float>> = BehaviorSubject.create()

    private lateinit var mPlotPoints: Array<Float>

    private var attrsLoaded: BehaviorSubject<Boolean> = BehaviorSubject.create()

    init {
        attrsLoaded.subscribe {
            if (it) {
                setupObservers()
            }
        }

        mAudioFileUiState.subscribe{ checkAttrs() }
        mSamplesReader.subscribe { checkAttrs() }
        mFileWaveViewStore.subscribe { checkAttrs() }

        setWillNotDraw(false)
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 15.0f
        typeface = Typeface.create("", Typeface.BOLD)
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    fun setAudioFileUiState(audioFileUiState: AudioFileUiState) {
        mAudioFileUiState.onNext(audioFileUiState)
    }

    fun setSamplesReader(samplesReader: (Int) -> Deferred<Array<Float>>) {
        mSamplesReader.onNext(samplesReader)
    }

    fun setFileWaveViewStore(fileWaveViewStore: FileWaveViewStore) {
        mFileWaveViewStore.onNext(fileWaveViewStore)
    }

    private fun getZoomLevel(): Int {
        if (!mAudioFileUiState.hasValue()) return 1
        return mAudioFileUiState.value.zoomLevelValue
    }

    fun zoomIn() {
        if (mFileWaveViewStore.value.zoomIn(mAudioFileUiState.value)) {
            handleZoom()
        }
    }

    fun zoomOut() {
        if (mFileWaveViewStore.value.zoomOut(mAudioFileUiState.value)) {
            handleZoom()
        }
    }

    private fun resetZoom() {
        if (mFileWaveViewStore.hasValue() && mAudioFileUiState.hasValue()) {
            mFileWaveViewStore.value.resetZoomLevel(mAudioFileUiState.value.path)
            handleZoom()
        }
    }

    private fun setupObservers() {
        mRawPoints.subscribe { ptsArr ->
            processPlotPoints(ptsArr)
        }

        mAudioFileUiState.value.displayPtsCount.subscribe {
            requestLayout()
        }
        mAudioFileUiState.value.zoomLevel.subscribe {
            handleZoom()
        }
    }

    private fun getPlotNumSamples(): Int {
        if (!mAudioFileUiState.hasValue()) return 0

        return mAudioFileUiState.value.displayPtsCountValue
    }

    private fun getPlotNumPts(): Int {
        val numSamples = getPlotNumSamples()

        val zoomLevel = getZoomLevel()
        return zoomLevel * numSamples
    }

    private fun fetchPointsToPlot() {
        if (!attrsLoaded.hasValue()) return

        val numPts = getPlotNumPts()

        mFileWaveViewStore.value.coroutineScope.launch {
            mRawPoints.onNext(mSamplesReader.value.apply(numPts).await())
        }
    }

    private fun processPlotPoints(rawPts: Array<Float>) {
        if (rawPts.isEmpty()) {
            return
        }

        val mean = rawPts.average()

        val maximum = rawPts.maxOrNull()

        val maxLevelInSamples = maximum ?: 3 * mean
        val maxToScale = height * 0.95

        mPlotPoints = rawPts.map { current ->
            if (maxLevelInSamples != 0) {
                ((current / maxLevelInSamples.toFloat()) * maxToScale.toFloat())
            } else 0.0f
        }.toTypedArray()

        invalidate()
    }

    private fun checkAttrs() {
        if (mAudioFileUiState.hasValue()
            && mFileWaveViewStore.hasValue()) {
            attrsLoaded.onNext(true)
        }
    }

    private fun handleZoom() {
        requestLayout()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (::mAudioWidgetSlider.isInitialized) {
            val sliderLeft = 0
            val sliderTop = 0
            mAudioWidgetSlider.layout(
                sliderLeft,
                sliderTop,
                sliderLeft + mAudioWidgetSlider.measuredWidth,
                sliderTop + mAudioWidgetSlider.measuredHeight
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (mFileWaveViewStore.hasValue()) {
            mFileWaveViewStore.value.updateMeasuredWidth(measuredWidth)
        }

        if (!mAudioFileUiState.hasValue()) return

        if (childCount == 1) {
            val child = getChildAt(0)
            if (child is AudioWidgetSlider && !::mAudioWidgetSlider.isInitialized) {
                mAudioWidgetSlider = child
            }
        }

        if (::mAudioWidgetSlider.isInitialized) {
            val sliderWidth = context.resources.getDimension(R.dimen.audio_view_slider_line_width)
            mAudioWidgetSlider.measure(
                MeasureSpec.makeMeasureSpec(ceil(sliderWidth).toInt(), MeasureSpec.EXACTLY),
                measuredHeight
            )
        }

        val samplesCount = getPlotNumSamples()

        val zoomLevel = getZoomLevel()
        val calculatedWidth = zoomLevel * samplesCount

        val roundedWidth = if (measuredWidth == 0 || calculatedWidth < measuredWidth) measuredWidth else calculatedWidth

        if (roundedWidth > 0) {
            fetchPointsToPlot()
        }

        setMeasuredDimension(roundedWidth, measuredHeight)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        resetZoom()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!::mPlotPoints.isInitialized) {
            return
        }

        val numPts = getPlotNumPts()
        val widthPtRatio = numPts / mPlotPoints.size
        val ptsDistance: Int = if (widthPtRatio >= 1) widthPtRatio else 1

        var currentPoint = 0

        mPlotPoints.forEach { item ->
            canvas.drawLine(currentPoint.toFloat(), height.toFloat(), currentPoint.toFloat(), (height - item), paint)
            currentPoint += ptsDistance
        }
    }
}
