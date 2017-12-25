package com.vuforia.samples.VuforiaSamples.AR

/**
 * Created by J on 2017-10-29.
 */

import java.lang.ref.WeakReference

import android.app.Activity
import android.os.Handler
import android.os.Message
import android.view.View

// 로딩화면의 대화창dialog 제어하는 클래스
class LoadingDialogHandler(activity : Activity) : Handler()
{
    private val mActivity: WeakReference<Activity>

    var mLoadingDialogContainer: View? = null

    // AR을 실행시키는 엑티비티를 weak레퍼런스로 지정
    init {
        mActivity = WeakReference(activity)
    }

    // 디버깅을 위해 헨들러 메세지 출력
    override fun handleMessage(msg: Message)
    {
        val imageTargets = mActivity.get() ?: return

        if (msg.what == SHOW_LOADING_DIALOG)
        {
            mLoadingDialogContainer!!.visibility = View.VISIBLE

        }
        else if (msg.what == HIDE_LOADING_DIALOG)
        {
            mLoadingDialogContainer!!.visibility = View.GONE
        }
    }

    // static 변수이므로 1번만 실행되도록 설정
    companion object {
        // Constants for Hiding/Showing Loading dialog
        // 로딩 대화창의 상태 숨김/표시를 나타낼 상수
        val HIDE_LOADING_DIALOG : Int = 0
        val SHOW_LOADING_DIALOG : Int = 1
    }

}
