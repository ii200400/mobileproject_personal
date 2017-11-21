package com.vuforia.samples.VuforiaSamples.AR

/**
 * Created by J on 2017-10-13.
 */

// Used to send back to the activity any error during vuforia processes
// AR 동작 중에 일어날 수 있는 오류를 저장해놓은 클래스
// 액티비티나 다른 AR 클래스에서 요청시 오류 상태를 반환한다.
class AppException(var mCode : Int = -1, var mString : String = "") : Exception(mString)
{
    // 시리얼 고유식별자
    val serialVersionUID : Long = 2L

    // AR 동작 중에 일어날 수 있는 오류들을 상수로 정의
    val INITIALIZATION_FAILURE : Int = 0    // 초기화 실패
    val VUFORIA_ALREADY_INITIALIZATED : Int = 1 // 이미 뷰포리아가 초기화되어있을때 초기화 시도시
    val TRACKERS_INITIALIZATION_FAILURE : Int = 2   // 트래커 초기화 실패
    val LOADING_TRACKERS_FAILURE : Int = 3  // 트래커 데이터 로드 실패
    val UNLOADING_TRACKERS_FAILURE : Int = 4    // 트래커 데이터 로드가 안되있는데 접근시
    val TRACKERS_DEINITIALIZATION_FAILURE : Int = 5 // 트래커 자원 할당 해제 실패
    val CAMERA_INITIALIZATION_FAILURE : Int = 6 // 카메라 초기화 실패
    val SET_FOCUS_MODE_FAILURE : Int = 7    // 카메라 focus 모드 세팅 실패
    val ACTIVATE_FLASH_FAILURE : Int = 8    // 플래쉬 작동 실패

    fun getCode(): Int
    {
        return mCode
    }

    fun getString(): String
    {
        return mString
    }
}