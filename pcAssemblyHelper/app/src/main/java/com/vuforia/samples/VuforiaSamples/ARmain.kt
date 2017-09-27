package com.vuforia.samples.VuforiaSamples

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

// 나중에 지워야함
import com.vuforia.samples.SampleApplication.SampleApplicationSession
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler

// AR 구현할 클래스
class ARmain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_armain)

        // 여기부터가 startLoadingAnimation 부분
        // SampleApplicationControl을 implement해야지 빨간줄 사라진다.
        var vuforiaAppSession = SampleApplicationSession(this)

        var mUILayout = View.inflate(this, R.layout.camera_overlay,null) as RelativeLayout

        mUILayout.setVisibility(View.VISIBLE)
        mUILayout.setBackgroundColor(Color.BLACK)

        var loadingDialogHandler = LoadingDialogHandler(this)
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator)

        // Shows the loading indicator at start
        loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG)

        // Adds the inflated layout to the view
        addContentView(mUILayout,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT))
        // 여기까지가 startLoadingAnimation 부분
    }
}
