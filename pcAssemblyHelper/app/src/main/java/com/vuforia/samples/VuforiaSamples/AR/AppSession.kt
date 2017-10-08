package com.vuforia.samples.VuforiaSamples.AR

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Point
import android.os.AsyncTask
import android.os.AsyncTask.Status.FINISHED
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.OrientationEventListener
import android.view.WindowManager

import com.vuforia.CameraCalibration
import com.vuforia.CameraDevice
import com.vuforia.Device
import com.vuforia.INIT_ERRORCODE
import com.vuforia.INIT_FLAGS
import com.vuforia.Matrix44F
import com.vuforia.Renderer
import com.vuforia.State
import com.vuforia.Tool
import com.vuforia.Vec2I
import com.vuforia.VideoBackgroundConfig
import com.vuforia.VideoMode
import com.vuforia.Vuforia
import com.vuforia.Vuforia.UpdateCallbackInterface

// 나중에 삭제해야함
import com.vuforia.samples.SampleApplication.SampleApplicationControl
import com.vuforia.samples.SampleApplication.SampleApplicationException
import com.vuforia.samples.VuforiaSamples.R

class AppSession(var mSessionControl: SampleApplicationControl) : UpdateCallbackInterface
{
    // 로그 출력을 위한 변수
    // 디버깅 끝나면 다 지울 예정
    val LOGTAG = "SampleAppSession"

    // Reference to the current activity
    lateinit var mActivity : Activity
    //var mSessionControl : SampleApplicationControl // 생성자로 편입

    // Flags
    var mStarted = false
    var mCameraRunning = false

    // The async tasks to initialize the Vuforia SDK:
    private var mInitVuforiaTask : InitVuforiaTask? = null
    private var mLoadTrackerTask : LoadTrackerTask? = null

    // An object used for synchronizing Vuforia initialization, dataset loading
    // and the Android onDestroy() life cycle event. If the application is
    // destroyed while a data set is still being loaded, then we wait for the
    // loading operation to finish before shutting down Vuforia:
    val mShutdownLock = Object()

    // Vuforia initialization flags:
    var mVuforiaFlags : Int = 0

    // Holds the camera configuration to use upon resuming
    var mCamera : Int = CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT

    // 반드시 오버라이드 해야하는 함수
    override fun Vuforia_onUpdate(s : State?)
    {
        mSessionControl.onVuforiaUpdate(s)
    }

    // Initializes Vuforia and sets up preferences.
    fun initAR(activity: Activity, sco: Int)
    {
        var screenOrientation : Int = sco
        var vuforiaException: SampleApplicationException? = null
        mActivity = activity

        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO)
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        // Use an OrientationChangeListener here to capture all orientation changes.  Android
        // will not send an Activity.onConfigurationChanged() callback on a 180 degree rotation,
        // ie: Left Landscape to Right Landscape.  Vuforia needs to react to this change and the
        // SampleApplicationSession needs to update the Projection Matrix.

        val orientationEventListener = object : OrientationEventListener(mActivity)
        {
            var mLastRotation = -1

            override fun onOrientationChanged(i : Int)
            {
                val activityRotation : Int = mActivity.windowManager.defaultDisplay.rotation

                if (mLastRotation != activityRotation)
                {
                    mLastRotation = activityRotation
                }
            }
        }

        if (orientationEventListener.canDetectOrientation())
            orientationEventListener.enable()

        // Apply screen orientation
        mActivity.requestedOrientation = screenOrientation

        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright:
        mActivity.window.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mVuforiaFlags = INIT_FLAGS.GL_20

        // Initialize Vuforia SDK asynchronously to avoid blocking the
        // main (UI) thread.
        //
        // NOTE: This task instance must be created and invoked on the
        // UI thread and it can be executed only once!
        if (mInitVuforiaTask != null)
        {
            val logMessage = "Cannot initialize SDK twice"
            vuforiaException = SampleApplicationException(
                    SampleApplicationException.VUFORIA_ALREADY_INITIALIZATED, logMessage)
            Log.e(LOGTAG, logMessage)
        }

        if (vuforiaException == null)
        {
            try {
                mInitVuforiaTask = InitVuforiaTask()
                mInitVuforiaTask!!.execute()
            } catch (e: Exception) {
                val logMessage = "Initializing Vuforia SDK failed"
                vuforiaException = SampleApplicationException(
                        SampleApplicationException.INITIALIZATION_FAILURE,
                        logMessage)
                Log.e(LOGTAG, logMessage)
            }

        }

        if (vuforiaException != null)
            mSessionControl.onInitARDone(vuforiaException)
    }

    // Starts Vuforia, initialize and starts the camera and start the trackers
    @Throws(SampleApplicationException::class)
    fun startAR(camera : Int) //throws SampleApplicationException
    {
        var error: String

        // 아래 오류 메세지는 삭제할 예정
        if (mCameraRunning)
        {
            error = "Camera already running, unable to open again"
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        mCamera = camera
        if (!CameraDevice.getInstance().init(camera))
        {
            error = "Unable to open camera device: " + camera
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        if (!CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT))
        {
            error = "Unable to set video mode"
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        if (!CameraDevice.getInstance().start())
        {
            error = "Unable to start camera device: " + camera
            Log.e(LOGTAG, error)
            throw SampleApplicationException(
                    SampleApplicationException.CAMERA_INITIALIZATION_FAILURE, error)
        }

        mSessionControl.doStartTrackers()

        mCameraRunning = true

        if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
        {
            if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL)
        }
    }

    // Stops any ongoing initialization, stops Vuforia
    @Throws(SampleApplicationException::class)
    fun stopAR()
    {
        /*
        // 자바의 InitVuforiaTask.Status 가 제대로 안나와서 빨간줄이 뜬다.
        // Cancel potentially running tasks
        if (mInitVuforiaTask != null && mInitVuforiaTask!!.getStatus() != InitVuforiaTask.Status.FINISHED)
        {
            mInitVuforiaTask!!.cancel(true)
            mInitVuforiaTask = null
        }

        if (mLoadTrackerTask != null && mLoadTrackerTask!!.getStatus() != LoadTrackerTask.Status.FINISHED)
        {
            mLoadTrackerTask!!.cancel(true)
            mLoadTrackerTask = null
        }
        */

        mInitVuforiaTask = null
        mLoadTrackerTask = null

        mStarted = false

        stopCamera()

        // Ensure that all asynchronous operations to initialize Vuforia
        // and loading the tracker datasets do not overlap:
        synchronized(mShutdownLock)
        {
            val unloadTrackersResult: Boolean
            val deinitTrackersResult: Boolean

            // Destroy the tracking data set:
            unloadTrackersResult = mSessionControl.doUnloadTrackersData()

            // Deinitialize the trackers:
            deinitTrackersResult = mSessionControl.doDeinitTrackers()

            // Deinitialize Vuforia SDK:
            Vuforia.deinit()

            if (!unloadTrackersResult)
                throw SampleApplicationException(
                        SampleApplicationException.UNLOADING_TRACKERS_FAILURE,
                        "Failed to unload trackers\' data")

            if (!deinitTrackersResult)
                throw SampleApplicationException(
                        SampleApplicationException.TRACKERS_DEINITIALIZATION_FAILURE,
                        "Failed to deinitialize trackers")

        }
    }

    // Resumes Vuforia, restarts the trackers and the camera
    @Throws(SampleApplicationException::class)
    fun resumeAR()
    {
        // Vuforia-specific resume operation
        Vuforia.onResume()

        if (mStarted)
        {
            startAR(mCamera)
        }
    }

    // Pauses Vuforia and stops the camera
    @Throws(SampleApplicationException::class)
    fun pauseAR()
    {
        if (mStarted)
        {
            stopCamera()
        }

        Vuforia.onPause()
    }

    // Manages the configuration changes
    fun onConfigurationChanged()
    {
        Device.getInstance().setConfigurationChanged()
    }

    // Methods to be called to handle lifecycle
    fun onResume()
    {
        Vuforia.onResume()
    }

    fun onPause()
    {
        Vuforia.onPause()
    }

    fun onSurfaceChanged(width: Int, height: Int)
    {
        Vuforia.onSurfaceChanged(width, height)
    }

    fun onSurfaceCreated()
    {
        Vuforia.onSurfaceCreated()
    }

    // An async task to initialize Vuforia asynchronously.
    private inner class InitVuforiaTask : AsyncTask<Void, Int, Boolean>()
    {
        // Initialize with invalid value:
        var mProgressValue : Int = -1

        // 반드시 오버라이드 해야하는 함수
        protected override fun doInBackground(vararg params: Void): Boolean
        {
            // Prevent the onDestroy() method to overlap with initialization:
            synchronized(mShutdownLock)
            {
                Vuforia.setInitParameters(mActivity, mVuforiaFlags, "AW0IwM3/////AAAAGSFlz9euJEN2tDRjCc5hrsUOLCOEYERKT8ECJEeHssicOqSFVF7g+lMBFQb9eqiRKnFZYt+lNhyf+x1FMd9k0SL5d6/+Xm4HiAKqzIVPySe4BAfARZhCVmroVqzmgUeUaVoZOh81/Gs7GbsW0epxzWPkGU8wFJPxMC/vZ69ziB8a7jaqqKRmjORMnThV7QmiPVaBAerHjls73RQ30cFEFeAvWnoJiuCERHHiYgjKNRUBp+pyN9CcvsSGWD1h2mDEyPM+ckWWRZ9Rtob7RabN3YGlOHj7eFXYOSvbmXu2MhSwKrvPZC0bJ0+9VCnOyA3uQgWy3q6cKsMCLgzYOUe1jW1pfTcU+2hJ9CH9cd2GnWd9")

                do {
                    // Vuforia.init() blocks until an initialization step is
                    // complete, then it proceeds to the next step and reports
                    // progress in percents (0 ... 100%).
                    // If Vuforia.init() returns -1, it indicates an error.
                    // Initialization is done when progress has reached 100%.
                    mProgressValue = Vuforia.init()

                    // Publish the progress value:
                    publishProgress(mProgressValue)

                    // We check whether the task has been canceled in the
                    // meantime (by calling AsyncTask.cancel(true)).
                    // and bail out if it has, thus stopping this thread.
                    // This is necessary as the AsyncTask will run to completion
                    // regardless of the status of the component that
                    // started is.
                } while (!isCancelled && mProgressValue >= 0
                        && mProgressValue < 100)

                return mProgressValue > 0
            }
        }

        protected fun onProgressUpdate(vararg values: Int)
        {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }

        override fun onPostExecute(result: Boolean?)
        {
            // Done initializing Vuforia, proceed to next application
            // initialization status:

            var vuforiaException: SampleApplicationException? = null

            if (result!!)
            {
                Log.d(LOGTAG, "InitVuforiaTask.onPostExecute: Vuforia " + "initialization successful")

                val initTrackersResult: Boolean
                initTrackersResult = mSessionControl.doInitTrackers()

                if (initTrackersResult)
                {
                    try {
                        mLoadTrackerTask = LoadTrackerTask()
                        mLoadTrackerTask!!.execute()
                    } catch (e: Exception) {
                        val logMessage = "Loading tracking data set failed"
                        vuforiaException = SampleApplicationException(
                                SampleApplicationException.LOADING_TRACKERS_FAILURE,
                                logMessage)
                        Log.e(LOGTAG, logMessage)
                        mSessionControl.onInitARDone(vuforiaException)
                    }

                }
                else
                {
                    vuforiaException = SampleApplicationException(
                            SampleApplicationException.TRACKERS_INITIALIZATION_FAILURE,
                            "Failed to initialize trackers")
                    mSessionControl.onInitARDone(vuforiaException)
                }
            }
            else
            {
                val logMessage: String

                // NOTE: Check if initialization failed because the device is
                // not supported. At this point the user should be informed
                // with a message.
                logMessage = getInitializationErrorString(mProgressValue)

                // Log error:
                Log.e(LOGTAG, "InitVuforiaTask.onPostExecute: " + logMessage
                        + " Exiting.")

                // Send Vuforia Exception to the application and call initDone
                // to stop initialization process
                vuforiaException = SampleApplicationException(
                        SampleApplicationException.INITIALIZATION_FAILURE,
                        logMessage)
                mSessionControl.onInitARDone(vuforiaException)
            }
        }
    }

    // An async task to load the tracker data asynchronously.
    private inner class LoadTrackerTask : AsyncTask<Void, Int, Boolean>()
    {
        // 반드시 오버라이드 해야하는 함수
        protected override fun doInBackground(vararg params: Void?): Boolean
        {
            // Prevent the onDestroy() method to overlap:
            synchronized(mShutdownLock)
            {
                // Load the tracker data set:
                return mSessionControl.doLoadTrackersData()
            }
        }

        protected override fun onPostExecute(result: Boolean?)
        {
            var vuforiaException: SampleApplicationException? = null

            Log.d(LOGTAG, "LoadTrackerTask.onPostExecute: execution "
                    + if (result!!) "successful" else "failed")

            if ((!result)!!)
            {
                val logMessage = "Failed to load tracker data."
                // Error loading dataset
                Log.e(LOGTAG, logMessage)
                vuforiaException = SampleApplicationException(
                        SampleApplicationException.LOADING_TRACKERS_FAILURE,
                        logMessage)
            }
            else
            {
                // Hint to the virtual machine that it would be a good time to
                // run the garbage collector:
                //
                // NOTE: This is only a hint. There is no guarantee that the
                // garbage collector will actually be run.
                System.gc()

                Vuforia.registerCallback(this@AppSession)

                mStarted = true
            }

            // Done loading the tracker, update application status, send the
            // exception to check errors
            mSessionControl.onInitARDone(vuforiaException)
        }
    }

    // Returns the error message for each error code
    private fun getInitializationErrorString(code: Int): String
    {
        if (code == INIT_ERRORCODE.INIT_DEVICE_NOT_SUPPORTED)
            return mActivity.getString(R.string.INIT_ERROR_DEVICE_NOT_SUPPORTED)
        if (code == INIT_ERRORCODE.INIT_NO_CAMERA_ACCESS)
            return mActivity.getString(R.string.INIT_ERROR_NO_CAMERA_ACCESS)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_MISSING_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_MISSING_KEY)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_INVALID_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_INVALID_KEY)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
        if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_CANCELED_KEY)
            return mActivity.getString(R.string.INIT_LICENSE_ERROR_CANCELED_KEY)

        return if (code == INIT_ERRORCODE.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
            mActivity.getString(R.string.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
        else {
            mActivity.getString(R.string.INIT_LICENSE_ERROR_UNKNOWN_ERROR)
        }
    }

    fun stopCamera()
    {
        if (mCameraRunning)
        {
            mSessionControl.doStopTrackers()
            mCameraRunning = false
            CameraDevice.getInstance().stop()
            CameraDevice.getInstance().deinit()
        }
    }

    // Returns true if Vuforia is initialized, the trackers started and the
    // tracker data loaded
    private fun isARRunning(): Boolean
    {
        return mStarted
    }
}
