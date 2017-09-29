package com.vuforia.samples.VuforiaSamples

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import java.util.*

// 나중에 지워야함
import com.vuforia.samples.SampleApplication.SampleApplicationSession
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler
import com.vuforia.samples.SampleApplication.utils.Texture

// AR 구현할 클래스
class ARmain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_armain)

        // 여기부터가 startLoadingAnimation 부분
        // SampleApplicationControl을 implement해야지 빨간줄 사라진다.
        // 추후 재설계 예정
        var vuforiaAppSession = SampleApplicationSession(this)

        // AR에 사용할 카메라용 레이아웃 설정
        var mUILayout = View.inflate(this, R.layout.camera_overlay,null) as RelativeLayout

        // 레이아웃의 visibility on
        mUILayout.setVisibility(View.VISIBLE)

        // 레이아웃의 배경색을 검정색으로 설정
        mUILayout.setBackgroundColor(Color.BLACK)

        var loadingDialogHandler = LoadingDialogHandler(this)

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator)

        // Shows the loading indicator at start
        loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG)

        // Adds the inflated layout to the view
        addContentView(mUILayout,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT))
        // 여기까지가 startLoadingAnimation 부분

        // 마커로 사용할 xml 파일을 담을 배열
        var mDatasetStrings = arrayListOf<String>()

        // 사용할 마커를 배열에 추가
        mDatasetStrings.add("StonesAndChips.xml")
        mDatasetStrings.add("Tarmac.xml")

        // 추후 재설계해야할 initAR 함수
        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        // 추후 재설계해야할 클래스 GestureListener
        var mGestureDetector : GestureDetector = GestureDetector(this, GestureListener())

        // Load any sample specific textures:
        // 마커를 인식하면 띄울 텍스쳐를 저장할 벡터
        var mTextures : Vector<Texture> = Vector<Texture>()

        // 마커 인식하면 띄울 텍스쳐 추가
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png", assets))
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png", assets))
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png", assets))
        mTextures.add(Texture.loadTextureFromApk("ImageTargets/Buildings.jpeg", assets))

        // 좀더 확인해야하는 부분
        var mIsDroidDevice : Boolean = android.os.Build.MODEL.toLowerCase().startsWith("droid")
    }
}
