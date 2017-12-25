package com.vuforia.samples.VuforiaSamples.AR

/**
 * Created by J on 2017-10-13.
 */

import com.vuforia.State
import com.vuforia.samples.SampleApplication.SampleApplicationException

// AppSession 클래스를 사용하는 액티비티에서 구현해야할 인터페이스
// AR을 실행시키는 액티비티가 하나라면 인터페이스 없이 액티비티에 구현해도
// 상관없지만 재사용 및 추가기능을 위해 인터페이스로 만든다.
interface AppControl
{
    // To be called to initialize the trackers
    // 트래커를 초기화하는 함수
    // 반환값은 초기화 성공 여부
    fun doInitTrackers(): Boolean

    // To be called to load the trackers' data
    // 트래커 데이터를 로드하는 함수
    // 리턴값은 로드 성공 여부
    fun doLoadTrackersData(): Boolean

    // To be called to start tracking with the initialized trackers and their
    // loaded data
    // 초기화 및 로드된 트래커를 트래킹 시작하는 함수
    // 리턴값은 트래커 시작 성공 여부
    fun doStartTrackers(): Boolean

    // To be called to stop the trackers
    // 트래커를 중지시키는 함수
    // 리턴값은 중지 성공 여부
    fun doStopTrackers(): Boolean

    // To be called to destroy the trackers' data
    // 트래커 데이터를 삭제하는 함수
    // 리턴값은 삭제 성공 여부
    fun doUnloadTrackersData(): Boolean

    // To be called to deinitialize the trackers
    // 트래커에게 할당된 자원을 해제시키는 함수
    // 리턴값은 자원 해제 성공 여부
    fun doDeinitTrackers(): Boolean

    // This callback is called after the Vuforia initialization is complete,
    // the trackers are initialized, their data loaded and
    // tracking is ready to start
    // AR 실행이 완료되고나서 호출되는 함수
    // 뷰포리아 초기화, 트래커 초기화, 트래커 데이터 로드, 트래커 실행
    // 모두 완료되어야 AR 실행이 완료된 것이다.
    fun onInitARDone(e: SampleApplicationException)

    // This callback is called every cycle
    // 모든 AR 관련 동작에서 자동 호출되는 함수
    // 현재의 상태를 뷰포리아에 업데이트 시킨다.
    fun onVuforiaUpdate(state: State)
}