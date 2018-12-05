package org.cocos2d.opengl;

import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.types.CGSize;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.vtd.cocos2d.GLSurfaceView;
import com.vtd.cocos2d.MessageBasedTimer;
import com.vtd.cocos2d.TimeSpan;

public class CCGLSurfaceView extends GLSurfaceView {
	private static final int VIEWID = 0x1235;
    // private static final String LOG_TAG = CCGLSurfaceView.class.getSimpleName();
    private CCTouchDispatcher mDispatcher;
	//watchdog timer
	private MessageBasedTimer mWatchdogTimer;
	//

    public CGSize frame;

    public CCGLSurfaceView(Context context) {
        super(context);

        CCDirector.theApp = (Activity) context;

        mDispatcher = CCTouchDispatcher.sharedDispatcher();

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setId(VIEWID);

		//add watchdog timer check createSurface
		this.mWatchdogTimer = new MessageBasedTimer();
		this.mWatchdogTimer.setHandler(new Handler());
		this.mWatchdogTimer.setInterval(TimeSpan.fromSeconds(1L));
		this.mWatchdogTimer.setListener(new MessageBasedTimer.Listener() {
			@Override
			public void onTimerElapsed() {
				SurfaceHolder holder = CCGLSurfaceView.this.getHolder();
				if (holder == null || holder.getSurface() == null) return;
				if (CCGLSurfaceView.this.hasGLSurface() && CCGLSurfaceView.this.canRender()) return;
				CCGLSurfaceView.this.surfaceChanged(holder, PixelFormat.TRANSLUCENT, CCGLSurfaceView.this.getWidth(), CCGLSurfaceView.this.getHeight());
			}
		});
		//

        // add this to resolve Samsung's Galaxy opengl problem
        //  here for reference.
        // http://www.anddev.org/samsung_galaxy_odd_ogl_es_hardware_acceleration_resolved-t8511.html
        /* need a real machine to test
        this.setEGLConfigChooser(
        		new GLSurfaceView.EGLConfigChooser() {
        			public EGLConfig chooseConfig(EGL10 egl,EGLDisplay display) {
        				int[] attributes=new int[]{
        						//EGL10.EGL_RED_SIZE,
        						//5,
        						//EGL10.EGL_BLUE_SIZE,
        						//5,
        						//EGL10.EGL_GREEN_SIZE,
        						//6,
        						EGL10.EGL_DEPTH_SIZE,
        						16,
        						EGL10.EGL_NONE
        				};
        				EGLConfig[] configs=new EGLConfig[1];
        				int[] result=new int[1];
        				egl.eglChooseConfig(display,attributes,configs,1,result);
        				return configs[0];
        			}
        		}
        );*/
    }

	@Override
	public void onResume() {
		super.onResume();
		this.mWatchdogTimer.start();
	}

	@Override
	public void onPause() {
		this.mWatchdogTimer.stop();
		super.onPause();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.mWatchdogTimer.stop();
		super.surfaceDestroyed(holder);
	}

	@Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    	super.onLayout(changed, left, top, right, bottom);
    	frame = CGSize.make(right - left, bottom - top);
    }        

    @Override
    public boolean onTouchEvent(MotionEvent event) {
  	
    	mDispatcher.queueMotionEvent(event);
    	
//		switch (event.getAction()) {
//		case MotionEvent.ACTION_CANCEL:
//			mDispatcher.touchesCancelled(event);
//			break;
//		case MotionEvent.ACTION_DOWN:
//			mDispatcher.touchesBegan(event);
//			break;
//		case MotionEvent.ACTION_MOVE:
//			mDispatcher.touchesMoved(event);
//			break;
//		case MotionEvent.ACTION_UP:
//			mDispatcher.touchesEnded(event);
//			break;
//		}
		
		synchronized (CCDirector.sharedDirector()) {
			try {
				CCDirector.sharedDirector().wait(20L);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
    	
        return true;
    }
}

