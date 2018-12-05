package com.vtd.cocos2d;

import android.content.Context;
import android.opengl.GLDebugHelper;
import android.opengl.GLES10;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.Writer;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {
	private static final String TAG = "GLSurfaceView";
	private static final boolean LOG_ATTACH_DETACH = false;
	private static final boolean LOG_THREADS = false;
	private static final boolean LOG_PAUSE_RESUME = false;
	private static final boolean LOG_SURFACE = false;
	private static final boolean LOG_RENDERER = false;
	private static final boolean LOG_RENDERER_DRAW_FRAME = false;
	private static final boolean LOG_EGL = false;
	public static final int RENDERMODE_WHEN_DIRTY = 0;
	public static final int RENDERMODE_CONTINUOUSLY = 1;
	public static final int DEBUG_CHECK_GL_ERROR = 1;
	public static final int DEBUG_LOG_GL_CALLS = 2;
	private static final GLThreadManager sGLThreadManager = new GLThreadManager();
	private final WeakReference<GLSurfaceView> mThisWeakRef = new WeakReference(
			this);
	private GLThread mGLThread;
	private Renderer mRenderer;
	private boolean mDetached;
	private EGLConfigChooser mEGLConfigChooser;
	private EGLContextFactory mEGLContextFactory;
	private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
	private GLWrapper mGLWrapper;
	private int mDebugFlags;
	private int mEGLContextClientVersion;
	private boolean mPreserveEGLContextOnPause;
	private boolean mNeedsSwap = true;

	public GLSurfaceView(Context context) {
		super(context);
		init();
	}

	public GLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	protected void finalize() throws Throwable {
		try {
			if (this.mGLThread != null) {
				this.mGLThread.requestExitAndWait();
			}
		} finally {
			super.finalize();
		}
	}

	private void init() {
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
	}

	public void setGLWrapper(GLWrapper glWrapper) {
		this.mGLWrapper = glWrapper;
	}

	public void setDebugFlags(int debugFlags) {
		this.mDebugFlags = debugFlags;
	}

	public int getDebugFlags() {
		return this.mDebugFlags;
	}

	public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
		this.mPreserveEGLContextOnPause = preserveOnPause;
	}

	public boolean getPreserveEGLContextOnPause() {
		return this.mPreserveEGLContextOnPause;
	}

	public void setRenderer(Renderer renderer) {
		checkRenderThreadState();
		if (this.mEGLConfigChooser == null) {
			this.mEGLConfigChooser = new SimpleEGLConfigChooser(true);
		}
		if (this.mEGLContextFactory == null) {
			this.mEGLContextFactory = new DefaultContextFactory();
		}
		if (this.mEGLWindowSurfaceFactory == null) {
			this.mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
		}
		this.mRenderer = renderer;
		this.mGLThread = new GLThread(this.mThisWeakRef);
		this.mGLThread.start();
	}

	public void setEnterFrameListener(FrameEventListener listener){
		mGLThread.setFrameEventListener(listener);
	}

	public void setEGLContextFactory(EGLContextFactory factory) {
		checkRenderThreadState();
		this.mEGLContextFactory = factory;
	}

	public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
		checkRenderThreadState();
		this.mEGLWindowSurfaceFactory = factory;
	}

	public void setEGLConfigChooser(EGLConfigChooser configChooser) {
		checkRenderThreadState();
		this.mEGLConfigChooser = configChooser;
	}

	public void setEGLConfigChooser(boolean needDepth) {
		setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
	}

	public void setEGLConfigChooser(int redSize, int greenSize, int blueSize,
									int alphaSize, int depthSize, int stencilSize) {
		setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize,
				blueSize, alphaSize, depthSize, stencilSize));
	}

	public void setEGLContextClientVersion(int version) {
		checkRenderThreadState();
		this.mEGLContextClientVersion = version;
	}

	public void setRenderMode(int renderMode) {
		this.mGLThread.setRenderMode(renderMode);
	}

	public int getRenderMode() {
		return this.mGLThread.getRenderMode();
	}

	public void requestRender() {
		this.mGLThread.requestRender();
	}

	public void surfaceCreated(SurfaceHolder holder) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		this.mGLThread.surfaceDestroyed();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		if ((holder == null) || (holder.getSurface() == null)
				|| (!holder.getSurface().isValid())) {
			return;
		}

		this.mGLThread.surfaceCreated();
		this.mGLThread.onWindowResize(width, height);
	}

	public void onPause() {
		this.mGLThread.onPause();
	}

	public void onResume() {
		this.mGLThread.onResume();
	}

	public void queueEvent(Runnable r) {
		this.mGLThread.queueEvent(r);
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if ((this.mDetached) && (this.mRenderer != null)) {
			int renderMode = RENDERMODE_CONTINUOUSLY;
			if (this.mGLThread != null) {
				renderMode = this.mGLThread.getRenderMode();
			}
			this.mGLThread = new GLThread(this.mThisWeakRef);
			if (renderMode != RENDERMODE_CONTINUOUSLY) {
				this.mGLThread.setRenderMode(renderMode);
			}
			this.mGLThread.start();
		}
		this.mDetached = false;
	}

	protected void onDetachedFromWindow() {
		if (this.mGLThread != null) {
			this.mGLThread.requestExitAndWait();
		}
		this.mDetached = true;
		super.onDetachedFromWindow();
	}

	private void checkRenderThreadState() {
		if (this.mGLThread != null)
			throw new IllegalStateException(
					"setRenderer has already been called for this instance.");
	}

	public void setNeedsSwap() {
		this.mNeedsSwap = true;
	}

	public void clearNeedsSwap() {
		this.mNeedsSwap = false;
	}

	public boolean canRender() {
		return this.mRenderer != null && this.mGLThread != null;
	}

	protected boolean hasGLSurface() {
		GLThread thread = this.mGLThread;
		if (thread == null) {
			return false;
		}
		return thread.hasGLSurface();
	}

	private static class GLThreadManager {
		private static String TAG = "GLThreadManager";
		private boolean mGLESVersionCheckComplete;
		private int mGLESVersion;
		private boolean mGLESDriverCheckComplete;
		private boolean mMultipleGLESContextsAllowed;
		private boolean mLimitedGLESContexts;
		private static final int kGLES_20 = 0x20000;
		private static final String kMSM7K_RENDERER_PREFIX = "Q3Dimension MSM7500 ";
		private GLSurfaceView.GLThread mEglOwner;

		public synchronized void threadExiting(GLSurfaceView.GLThread thread) {
			if (LOG_THREADS) {
				Log.i("GLThread", "exiting tid=" + thread.getId());
			}
			thread.mExited = true;
			if (this.mEglOwner == thread) {
				this.mEglOwner = null;
			}
			notifyAll();
		}

		public boolean tryAcquireEglContextLocked(GLSurfaceView.GLThread thread) {
			if ((this.mEglOwner == thread) || (this.mEglOwner == null)) {
				this.mEglOwner = thread;
				notifyAll();
				return true;
			}
			checkGLESVersion();
			if (this.mMultipleGLESContextsAllowed) {
				return true;
			}

			if (this.mEglOwner != null) {
				this.mEglOwner.requestReleaseEglContextLocked();
			}
			return false;
		}

		public void releaseEglContextLocked(GLSurfaceView.GLThread thread) {
			if (this.mEglOwner == thread) {
				this.mEglOwner = null;
			}
			notifyAll();
		}

		public synchronized boolean shouldReleaseEGLContextWhenPausing() {
			return this.mLimitedGLESContexts;
		}

		public synchronized boolean shouldTerminateEGLWhenPausing() {
			checkGLESVersion();
			return !this.mMultipleGLESContextsAllowed;
		}

		public synchronized void checkGLDriver(GL10 gl) {
			if (!this.mGLESDriverCheckComplete) {
				checkGLESVersion();
				String renderer = gl.glGetString(GL10.GL_RENDERER);
				if (this.mGLESVersion < kGLES_20) {
					this.mMultipleGLESContextsAllowed = (!renderer
							.startsWith(kMSM7K_RENDERER_PREFIX));

					notifyAll();
				}
				this.mLimitedGLESContexts = (!this.mMultipleGLESContextsAllowed);

				this.mGLESDriverCheckComplete = true;
			}
		}

		private void checkGLESVersion() {
			if (!this.mGLESVersionCheckComplete) {
				this.mGLESVersion = 0;
				try {
					Class systemPropertiesClass = Class
							.forName("android.os.SystemProperties");
					Method getIntMethod = systemPropertiesClass.getMethod(
							"getInt",
							new Class[] { String.class, Integer.TYPE });

					Object value = getIntMethod.invoke(null, new Object[] {
							"ro.opengles.version", Integer.valueOf(0) });

					this.mGLESVersion = ((Integer) value).intValue();
				} catch (Exception ex) {
				}
				if (this.mGLESVersion >= kGLES_20) {
					this.mMultipleGLESContextsAllowed = true;
				}

				this.mGLESVersionCheckComplete = true;
			}
		}
	}

	static class LogWriter extends Writer {
		private StringBuilder mBuilder = new StringBuilder();

		public void close() {
			flushBuilder();
		}

		public void flush() {
			flushBuilder();
		}

		public void write(char[] buf, int offset, int count) {
			for (int i = 0; i < count; i++) {
				char c = buf[(offset + i)];
				if (c == '\n') {
					flushBuilder();
				} else
					this.mBuilder.append(c);
			}
		}

		private void flushBuilder() {
			if (this.mBuilder.length() > 0) {
				Log.v("GLSurfaceView", this.mBuilder.toString());
				this.mBuilder.delete(0, this.mBuilder.length());
			}
		}
	}

	static class GLThread extends Thread {
		private boolean mShouldExit;
		private boolean mExited;
		private boolean mRequestPaused;
		private boolean mPaused;
		private boolean mHasSurface;
		private boolean mSurfaceIsBad;
		private boolean mWaitingForSurface;
		private boolean mHaveEglContext;
		private boolean mHaveEglSurface;
		private boolean mShouldReleaseEglContext;
		private int mWidth;
		private int mHeight;
		private int mRenderMode;
		private boolean mRequestRender;
		private boolean mRenderComplete;
		private ArrayList<Runnable> mEventQueue = new ArrayList();
		private boolean mSizeChanged = true;
		private GLSurfaceView.EglHelper mEglHelper;
		private WeakReference<GLSurfaceView> mGLSurfaceViewWeakRef;
		FrameEventListener frameEventListener;
		private long lastTime;

		GLThread(WeakReference<GLSurfaceView> glSurfaceViewWeakRef) {
			this.mWidth = 0;
			this.mHeight = 0;
			this.mRequestRender = true;
			this.mRenderMode = RENDERMODE_CONTINUOUSLY;
			this.mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
		}

		public void run() {
			setName("GLThread " + getId());
			try {
				guardedRun();
			} catch (InterruptedException e) {
			} finally {
				GLSurfaceView.sGLThreadManager.threadExiting(this);
			}
		}

		private void stopEglSurfaceLocked() {
			if (this.mHaveEglSurface) {
				this.mHaveEglSurface = false;
				this.mEglHelper.destroySurface();
			}
		}

		private void stopEglContextLocked() {
			if (this.mHaveEglContext) {
				this.mEglHelper.finish();
				this.mHaveEglContext = false;
				GLSurfaceView.sGLThreadManager.releaseEglContextLocked(this);
			}
		}

		private void guardedRun() throws InterruptedException {
			Log.d(TAG, "guardedRun: ");
			this.mEglHelper = new GLSurfaceView.EglHelper(
					this.mGLSurfaceViewWeakRef);
			this.mHaveEglContext = false;
			this.mHaveEglSurface = false;
			try {
				GL10 gl = null;
				boolean createEglContext = false;
				boolean createEglSurface = false;
				boolean createGlInterface = false;
				boolean lostEglContext = false;
				boolean sizeChanged = false;
				boolean wantRenderNotification = false;
				boolean doRenderNotification = false;
				boolean askedToReleaseEglContext = false;
				int w = 0;
				int h = 0;
				Runnable event = null;
				while (true) {
					synchronized (GLSurfaceView.sGLThreadManager) {
						while(true) {
							if (this.mShouldExit) {
								return;
							}
							if (!this.mEventQueue.isEmpty()) {
								event = (Runnable) this.mEventQueue.remove(0);
								break;
							} else {
								boolean pausing = false;
								if (this.mPaused != this.mRequestPaused) {
									pausing = this.mRequestPaused;
									this.mPaused = this.mRequestPaused;
									GLSurfaceView.sGLThreadManager.notifyAll();
								}

								if (this.mShouldReleaseEglContext) {
									stopEglSurfaceLocked();
									stopEglContextLocked();
									this.mShouldReleaseEglContext = false;
									askedToReleaseEglContext = true;
								}

								if (lostEglContext) {
									stopEglSurfaceLocked();
									stopEglContextLocked();
									lostEglContext = false;
								}

								if ((!this.mHasSurface)
										&& (!this.mWaitingForSurface)) {
									if (this.mHaveEglSurface) {
										stopEglSurfaceLocked();
									}
									this.mWaitingForSurface = true;
									this.mSurfaceIsBad = false;
									GLSurfaceView.sGLThreadManager.notifyAll();
								}

								if ((this.mHasSurface) && (this.mWaitingForSurface)) {
									this.mWaitingForSurface = false;
									GLSurfaceView.sGLThreadManager.notifyAll();
								}

								if (doRenderNotification) {
									wantRenderNotification = false;
									doRenderNotification = false;
									this.mRenderComplete = true;
									GLSurfaceView.sGLThreadManager.notifyAll();
								}
								if (readyToDraw()) {
									if (!this.mHaveEglContext) {
										if (askedToReleaseEglContext) {
											askedToReleaseEglContext = false;
										} else if (GLSurfaceView.sGLThreadManager
												.tryAcquireEglContextLocked(this)) {
											try {
												this.mEglHelper.start();
											} catch (RuntimeException t) {
												GLSurfaceView.sGLThreadManager
														.releaseEglContextLocked(this);
												throw t;
											}
											this.mHaveEglContext = true;
											createEglContext = true;

											GLSurfaceView.sGLThreadManager
													.notifyAll();
										}
									}

									if ((this.mHaveEglContext)
											&& (!this.mHaveEglSurface)) {
										this.mHaveEglSurface = true;
										createEglSurface = true;
										createGlInterface = true;
										sizeChanged = true;
									}

									if (this.mHaveEglSurface) {
										if (this.mSizeChanged) {
											sizeChanged = true;
											w = this.mWidth;
											h = this.mHeight;
											wantRenderNotification = true;

											createEglSurface = true;

											this.mSizeChanged = false;
										}
										this.mRequestRender = false;
										GLSurfaceView.sGLThreadManager.notifyAll();
										break;
									}

								}

								GLSurfaceView.sGLThreadManager.wait();
							}
						}
					}
					if (event != null) {
						event.run();
						event = null;
					} else if (createEglSurface) {
						if (!this.mEglHelper.createSurface()) {
							synchronized (GLSurfaceView.sGLThreadManager) {
								this.mSurfaceIsBad = true;
								GLSurfaceView.sGLThreadManager.notifyAll();
							}
						} else
							createEglSurface = false;
					} else {
						if (createGlInterface) {
							gl = (GL10) this.mEglHelper.createGL();

							GLSurfaceView.sGLThreadManager.checkGLDriver(gl);
							createGlInterface = false;
						}

						if (createEglContext) {
							GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef
									.get();
							if (view != null) {
								view.mRenderer.onSurfaceCreated(gl,
										this.mEglHelper.mEglConfig);
							}
							createEglContext = false;
						}

						if (sizeChanged) {
							GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef
									.get();
							if (view != null) {
								view.mRenderer.onSurfaceChanged(gl, w, h);
							}
							sizeChanged = false;
						}

						GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef
								.get();
						if ((view != null) && (w > 0) && (h > 0)) {
							view.mRenderer.onDrawFrame(gl);
							dispatchDrawFrame();
							if (view.mNeedsSwap) {
								int swapError = this.mEglHelper.swap();
								switch (swapError) {
									case EGL10.EGL_SUCCESS:
										break;
									case EGL11.EGL_CONTEXT_LOST:
										lostEglContext = true;
										break;
									default:
										GLSurfaceView.EglHelper
												.logEglErrorAsWarning("GLThread",
														"eglSwapBuffers", swapError);

										synchronized (GLSurfaceView.sGLThreadManager) {
											this.mSurfaceIsBad = true;
											GLSurfaceView.sGLThreadManager
													.notifyAll();
										}
								}

								if (wantRenderNotification) {
									doRenderNotification = true;
								}
								view.mNeedsSwap = false;
							}
						}
					}

				}

			} finally {
				synchronized (GLSurfaceView.sGLThreadManager) {
					stopEglSurfaceLocked();
					stopEglContextLocked();
				}
			}
		}

		private void dispatchDrawFrame(){
			long currentTime = System.currentTimeMillis();
			if (lastTime == 0) {
				lastTime = currentTime;
			}
			long deltaTime = currentTime - lastTime;
			lastTime = currentTime;
		   	if (frameEventListener != null){
			    frameEventListener.onEnterFrame(deltaTime);
		    }
		}


		public boolean ableToDraw() {
			return (this.mHaveEglContext) && (this.mHaveEglSurface)
					&& (readyToDraw());
		}

		private boolean readyToDraw() {
			return (!this.mPaused) && (this.mHasSurface)
					&& (!this.mSurfaceIsBad) && (this.mWidth > 0)
					&& (this.mHeight > 0)
					&& ((this.mRequestRender) || (this.mRenderMode == RENDERMODE_CONTINUOUSLY));
		}

		public boolean hasGLSurface() {
			return this.mHasSurface;
		}

		public void setRenderMode(int renderMode) {
			if ((0 > renderMode) || (renderMode > 1)) {
				throw new IllegalArgumentException("renderMode");
			}
			synchronized (GLSurfaceView.sGLThreadManager) {
				this.mRenderMode = renderMode;
				GLSurfaceView.sGLThreadManager.notifyAll();
			}
		}

		public int getRenderMode() {
			synchronized (GLSurfaceView.sGLThreadManager) {
				return this.mRenderMode;
			}
		}

		public void requestRender() {
			synchronized (GLSurfaceView.sGLThreadManager) {
				this.mRequestRender = true;
				GLSurfaceView.sGLThreadManager.notifyAll();
			}
		}

		public void surfaceCreated() {
			synchronized (GLSurfaceView.sGLThreadManager) {
				this.mHasSurface = true;
				GLSurfaceView.sGLThreadManager.notifyAll();
				while ((this.mWaitingForSurface) && (!this.mExited))
					try {
						GLSurfaceView.sGLThreadManager.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
			}
		}

		public void surfaceDestroyed() {
			synchronized (GLSurfaceView.sGLThreadManager) {
				this.mHasSurface = false;
				GLSurfaceView.sGLThreadManager.notifyAll();
				while ((!this.mWaitingForSurface) && (!this.mExited))
					try {
						GLSurfaceView.sGLThreadManager.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
			}
		}

		public void onPause() {
			synchronized (GLSurfaceView.sGLThreadManager) {
				this.mRequestPaused = true;
				GLSurfaceView.sGLThreadManager.notifyAll();
				while ((!this.mExited) && (!this.mPaused)) {
					try {
						GLSurfaceView.sGLThreadManager.wait();
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}

		public void onResume() {
			synchronized (GLSurfaceView.sGLThreadManager) {
				this.mRequestPaused = false;
				this.mRequestRender = true;
				this.mRenderComplete = false;
				GLSurfaceView.sGLThreadManager.notifyAll();
				while ((!this.mExited) && (this.mPaused)
						&& (!this.mRenderComplete)) {
					try {
						GLSurfaceView.sGLThreadManager.wait();
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}

		public void onWindowResize(int w, int h) {
			synchronized (GLSurfaceView.sGLThreadManager) {
				if (this.mHasSurface) {
					this.mWidth = w;
					this.mHeight = h;
					this.mSizeChanged = true;
					this.mRequestRender = true;
					this.mRenderComplete = false;
					GLSurfaceView.sGLThreadManager.notifyAll();
				}
			}
		}

		public void requestExitAndWait() {
			synchronized (GLSurfaceView.sGLThreadManager) {
				this.mShouldExit = true;
				GLSurfaceView.sGLThreadManager.notifyAll();
				while (!this.mExited)
					try {
						GLSurfaceView.sGLThreadManager.wait();
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
			}
		}

		public void requestReleaseEglContextLocked() {
			this.mShouldReleaseEglContext = true;
			GLSurfaceView.sGLThreadManager.notifyAll();
		}

		public void queueEvent(Runnable r) {
			if (r == null) {
				throw new IllegalArgumentException("r must not be null");
			}
			synchronized (GLSurfaceView.sGLThreadManager) {
				this.mEventQueue.add(r);
				GLSurfaceView.sGLThreadManager.notifyAll();
			}
		}

		public void setFrameEventListener(FrameEventListener listener) {
			frameEventListener = listener;
		}
	}

	private static class EglHelper {
		private WeakReference<GLSurfaceView> mGLSurfaceViewWeakRef;
		EGL10 mEgl;
		EGLDisplay mEglDisplay;
		EGLSurface mEglSurface;
		EGLConfig mEglConfig;
		EGLContext mEglContext;
		private boolean mHasRetriedCreatingSurface = false;

		public EglHelper(WeakReference<GLSurfaceView> glSurfaceViewWeakRef) {
			this.mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
		}

		public void start() {
			this.mEgl = ((EGL10) EGLContext.getEGL());

			this.mEglDisplay = this.mEgl
					.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

			if (this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
				throw new RuntimeException("eglGetDisplay failed");
			}

			int[] version = new int[2];
			if (!this.mEgl.eglInitialize(this.mEglDisplay, version)) {
				throw new RuntimeException("eglInitialize failed");
			}
			GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef
					.get();
			if (view == null) {
				this.mEglConfig = null;
				this.mEglContext = null;
			} else {
				this.mEglConfig = view.mEGLConfigChooser.chooseConfig(
						this.mEgl, this.mEglDisplay);

				this.mEglContext = view.mEGLContextFactory.createContext(
						this.mEgl, this.mEglDisplay, this.mEglConfig);
			}
			if ((this.mEglContext == null)
					|| (this.mEglContext == EGL10.EGL_NO_CONTEXT)) {
				this.mEglContext = null;
				throwEglException("createContext");
			}

			this.mEglSurface = null;
		}

		public boolean createSurface() {
			if (this.mEgl == null) {
				throw new RuntimeException("egl not initialized");
			}
			if (this.mEglDisplay == null) {
				throw new RuntimeException("eglDisplay not initialized");
			}
			if (this.mEglConfig == null) {
				throw new RuntimeException("mEglConfig not initialized");
			}

			destroySurfaceImp();

			this.mEglSurface = null;
			GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef
					.get();
			if (view != null) {
				SurfaceHolder holder = view.getHolder();
				if ((holder != null) && (holder.getSurface() != null)
						&& (holder.getSurface().isValid())) {
					this.mEglSurface = view.mEGLWindowSurfaceFactory
							.createWindowSurface(this.mEgl, this.mEglDisplay,
									this.mEglConfig, holder);
				}
			}

			if ((this.mEglSurface == null)
					|| (this.mEglSurface == EGL10.EGL_NO_SURFACE)) {
				int error = this.mEgl.eglGetError();
				if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
					Log.e("EglHelper",
							"createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
				}
				return false;
			}

			if (!this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface,
					this.mEglSurface, this.mEglContext)) {
				logEglErrorAsWarning("EGLHelper", "eglMakeCurrent",
						this.mEgl.eglGetError());
				return false;
			}

			if (!this.mHasRetriedCreatingSurface) {
				String rendererName = GLES10.glGetString(GLES10.GL_RENDERER);
				if ((rendererName != null)
						&& (rendererName.equals("PowerVR SGX 540"))
						&& ((view.mEGLConfigChooser instanceof GLSurfaceView.ComponentSizeChooser))
						&& (((GLSurfaceView.ComponentSizeChooser) view.mEGLConfigChooser)
						.getMinAlphaSize() > 0)) {
					int previousMinAlphaSize = ((GLSurfaceView.ComponentSizeChooser) view.mEGLConfigChooser)
							.getMinAlphaSize();
					((GLSurfaceView.ComponentSizeChooser) view.mEGLConfigChooser)
							.setMinAlphaSize(0);
					boolean wasSurfaceCreated = false;
					try {
						wasSurfaceCreated = recreateSurface();
					} catch (Exception ex) {
					}
					if (!wasSurfaceCreated) {
						this.mHasRetriedCreatingSurface = true;
						((GLSurfaceView.ComponentSizeChooser) view.mEGLConfigChooser)
								.setMinAlphaSize(previousMinAlphaSize);
						return recreateSurface();
					}
				}
			}

			return true;
		}

		private boolean recreateSurface() {
			destroySurfaceImp();
			GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef
					.get();
			if ((view != null) && (this.mEglContext != null)) {
				SurfaceHolder holder = view.getHolder();
				if ((holder != null) && (holder.getSurface() != null)
						&& (holder.getSurface().isValid())) {
					view.mEGLContextFactory.destroyContext(this.mEgl,
							this.mEglDisplay, this.mEglContext);
				}
				this.mEglContext = null;
			}

			start();
			return createSurface();
		}

		GL createGL() {
			GL gl = this.mEglContext.getGL();
			GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef
					.get();
			if (view != null) {
				if (view.mGLWrapper != null) {
					gl = view.mGLWrapper.wrap(gl);
				}

				if ((view.mDebugFlags & (DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS)) != 0) {
					int configFlags = 0;
					Writer log = null;
					if ((view.mDebugFlags & DEBUG_CHECK_GL_ERROR) != 0) {
						configFlags |= GLDebugHelper.CONFIG_CHECK_GL_ERROR;
					}
					if ((view.mDebugFlags & DEBUG_LOG_GL_CALLS) != 0) {
						log = new GLSurfaceView.LogWriter();
					}
					gl = GLDebugHelper.wrap(gl, configFlags, log);
				}
			}
			return gl;
		}

		public int swap() {
			if ((this.mEgl == null) || (this.mEglDisplay == null)
					|| (this.mEglSurface == null)) {
				return EGL10.EGL_NOT_INITIALIZED;
			}

			if (!this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface)) {
				return this.mEgl.eglGetError();
			}
			return EGL10.EGL_SUCCESS;
		}

		public void destroySurface() {
			destroySurfaceImp();
		}

		private void destroySurfaceImp() {
			if ((this.mEglSurface != null)
					&& (this.mEglSurface != EGL10.EGL_NO_SURFACE)) {
				this.mEgl.eglMakeCurrent(this.mEglDisplay,
						EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
						EGL10.EGL_NO_CONTEXT);

				GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef
						.get();
				if (view != null) {
					view.mEGLWindowSurfaceFactory.destroySurface(this.mEgl,
							this.mEglDisplay, this.mEglSurface);
				}
				this.mEglSurface = null;
			}
		}

		public void finish() {
			if (this.mEglContext != null) {
				GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef
						.get();
				if (view != null) {
					view.mEGLContextFactory.destroyContext(this.mEgl,
							this.mEglDisplay, this.mEglContext);
				}
				this.mEglContext = null;
			}
			if (this.mEglDisplay != null) {
				this.mEgl.eglTerminate(this.mEglDisplay);
				this.mEglDisplay = null;
			}
		}

		private void throwEglException(String function) {
			throwEglException(function, this.mEgl.eglGetError());
		}

		public static void throwEglException(String function, int error) {
			String message = formatEglError(function, error);

			throw new RuntimeException(message);
		}

		public static void logEglErrorAsWarning(String tag, String function,
												int error) {
			Log.w(tag, formatEglError(function, error));
		}

		public static String formatEglError(String function, int error) {
			return function + " failed: " + getErrorString(error);
		}

		public static String getErrorString(int error) {
			switch (error) {
				case EGL10.EGL_SUCCESS:
					return "EGL_SUCCESS";
				case EGL10.EGL_NOT_INITIALIZED:
					return "EGL_NOT_INITIALIZED";
				case EGL10.EGL_BAD_ACCESS:
					return "EGL_BAD_ACCESS";
				case EGL10.EGL_BAD_ALLOC:
					return "EGL_BAD_ALLOC";
				case EGL10.EGL_BAD_ATTRIBUTE:
					return "EGL_BAD_ATTRIBUTE";
				case EGL10.EGL_BAD_CONFIG:
					return "EGL_BAD_CONFIG";
				case EGL10.EGL_BAD_CONTEXT:
					return "EGL_BAD_CONTEXT";
				case EGL10.EGL_BAD_CURRENT_SURFACE:
					return "EGL_BAD_CURRENT_SURFACE";
				case EGL10.EGL_BAD_DISPLAY:
					return "EGL_BAD_DISPLAY";
				case EGL10.EGL_BAD_MATCH:
					return "EGL_BAD_MATCH";
				case EGL10.EGL_BAD_NATIVE_PIXMAP:
					return "EGL_BAD_NATIVE_PIXMAP";
				case EGL10.EGL_BAD_NATIVE_WINDOW:
					return "EGL_BAD_NATIVE_WINDOW";
				case EGL10.EGL_BAD_PARAMETER:
					return "EGL_BAD_PARAMETER";
				case EGL10.EGL_BAD_SURFACE:
					return "EGL_BAD_SURFACE";
				case EGL11.EGL_CONTEXT_LOST:
					return "EGL_CONTEXT_LOST";
			}
			return Integer.toHexString(error);
		}
	}

	private class SimpleEGLConfigChooser extends
			GLSurfaceView.ComponentSizeChooser {
		public SimpleEGLConfigChooser(boolean withDepthBuffer) {
			super(4, 4, 4, 0, withDepthBuffer ? 16 : 0, 0);

			this.mRedSize = 5;
			this.mGreenSize = 6;
			this.mBlueSize = 5;
		}
	}

	private class ComponentSizeChooser extends GLSurfaceView.BaseConfigChooser {
		private int[] mValue;
		protected int mRedSize;
		protected int mGreenSize;
		protected int mBlueSize;
		protected int mAlphaSize;
		protected int mDepthSize;
		protected int mStencilSize;

		public ComponentSizeChooser(int redSize, int greenSize, int blueSize,
									int alphaSize, int depthSize, int stencilSize) {
			super(new int[] { EGL10.EGL_RED_SIZE, redSize, EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE,
					blueSize, EGL10.EGL_ALPHA_SIZE, 0, EGL10.EGL_DEPTH_SIZE, depthSize, EGL10.EGL_STENCIL_SIZE, stencilSize,
					EGL10.EGL_CONFIG_CAVEAT, EGL10.EGL_NONE, EGL10.EGL_NONE });

			this.mValue = new int[1];
			this.mRedSize = redSize;
			this.mGreenSize = greenSize;
			this.mBlueSize = blueSize;
			this.mAlphaSize = alphaSize;
			this.mDepthSize = depthSize;
			this.mStencilSize = stencilSize;
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
									  EGLConfig[] configs) {
			EGLConfig closestConfig = null;
			int closestDistance = 1000;

			for (EGLConfig config : configs) {
				int depth = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
				if (depth >= this.mDepthSize) {
					int stencil = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE,
							0);
					if (stencil >= this.mStencilSize) {
						int red = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE,
								0);
						int green = findConfigAttrib(egl, display, config,
								EGL10.EGL_GREEN_SIZE, 0);
						int blue = findConfigAttrib(egl, display, config,
								EGL10.EGL_BLUE_SIZE, 0);
						int alpha = findConfigAttrib(egl, display, config,
								EGL10.EGL_ALPHA_SIZE, 0);

						int distance = Math.abs(red - this.mRedSize)
								+ Math.abs(green - this.mGreenSize)
								+ Math.abs(blue - this.mBlueSize)
								+ Math.abs(alpha - this.mAlphaSize);

						if (distance < closestDistance) {
							closestDistance = distance;
							closestConfig = config;
						}
					}
				}
			}
			return closestConfig;
		}

		public int getMinAlphaSize() {
			return this.mAlphaSize;
		}

		public void setMinAlphaSize(int value) {
			this.mAlphaSize = value;
		}

		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
									 EGLConfig config, int attribute, int defaultValue) {
			if (egl.eglGetConfigAttrib(display, config, attribute, this.mValue)) {
				return this.mValue[0];
			}
			return defaultValue;
		}
	}

	private abstract class BaseConfigChooser implements
			GLSurfaceView.EGLConfigChooser {
		protected int[] mConfigSpec;

		public BaseConfigChooser(int[] configSpec) {
			this.mConfigSpec = filterConfigSpec(configSpec);
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
			int[] num_config = new int[1];
			if (!egl.eglChooseConfig(display, this.mConfigSpec, null, 0,
					num_config)) {
				throw new IllegalArgumentException("eglChooseConfig failed");
			}

			int numConfigs = num_config[0];

			if (numConfigs <= 0) {
				throw new IllegalArgumentException(
						"No configs match configSpec");
			}

			EGLConfig[] configs = new EGLConfig[numConfigs];
			if (!egl.eglChooseConfig(display, this.mConfigSpec, configs,
					numConfigs, num_config)) {
				throw new IllegalArgumentException("eglChooseConfig#2 failed");
			}
			EGLConfig config = chooseConfig(egl, display, configs);
			if (config == null) {
				throw new IllegalArgumentException("No config chosen");
			}
			return config;
		}

		abstract EGLConfig chooseConfig(EGL10 paramEGL10,
										EGLDisplay paramEGLDisplay, EGLConfig[] paramArrayOfEGLConfig);

		private int[] filterConfigSpec(int[] configSpec) {
			if (GLSurfaceView.this.mEGLContextClientVersion != 2) {
				return configSpec;
			}

			int len = configSpec.length;
			int[] newConfigSpec = new int[len + 2];
			System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
			newConfigSpec[(len - 1)] = EGL10.EGL_RENDERABLE_TYPE;
			newConfigSpec[len] = 4;
			newConfigSpec[(len + 1)] = EGL10.EGL_NONE;
			return newConfigSpec;
		}
	}

	public static abstract interface EGLConfigChooser {
		public abstract EGLConfig chooseConfig(EGL10 paramEGL10,
											   EGLDisplay paramEGLDisplay);
	}

	private static class DefaultWindowSurfaceFactory implements
			GLSurfaceView.EGLWindowSurfaceFactory {
		public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
											  EGLConfig config, Object nativeWindow) {
			EGLSurface result = null;
			try {
				result = egl.eglCreateWindowSurface(display, config,
						nativeWindow, null);
			} catch (IllegalArgumentException e) {
				Log.e("GLSurfaceView", "eglCreateWindowSurface", e);
			}
			return result;
		}

		public void destroySurface(EGL10 egl, EGLDisplay display,
								   EGLSurface surface) {
			egl.eglDestroySurface(display, surface);
		}
	}

	public static abstract interface EGLWindowSurfaceFactory {
		public abstract EGLSurface createWindowSurface(EGL10 paramEGL10,
													   EGLDisplay paramEGLDisplay, EGLConfig paramEGLConfig,
													   Object paramObject);

		public abstract void destroySurface(EGL10 paramEGL10,
											EGLDisplay paramEGLDisplay, EGLSurface paramEGLSurface);
	}

	private class DefaultContextFactory implements
			GLSurfaceView.EGLContextFactory {
		private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

		private DefaultContextFactory() {
		}

		public EGLContext createContext(EGL10 egl, EGLDisplay display,
										EGLConfig config) {
			int[] attrib_list = { this.EGL_CONTEXT_CLIENT_VERSION,
					GLSurfaceView.this.mEGLContextClientVersion, EGL10.EGL_NONE };

			return egl
					.eglCreateContext(
							display,
							config,
							EGL10.EGL_NO_CONTEXT,
							GLSurfaceView.this.mEGLContextClientVersion != 0 ? attrib_list
									: null);
		}

		public void destroyContext(EGL10 egl, EGLDisplay display,
								   EGLContext context) {
			if (!egl.eglDestroyContext(display, context)) {
				Log.e("DefaultContextFactory", "display:" + display
						+ " context: " + context);

				GLSurfaceView.EglHelper.throwEglException("eglDestroyContex",
						egl.eglGetError());
			}
		}
	}

	public static abstract interface EGLContextFactory {
		public abstract EGLContext createContext(EGL10 paramEGL10,
												 EGLDisplay paramEGLDisplay, EGLConfig paramEGLConfig);

		public abstract void destroyContext(EGL10 paramEGL10,
											EGLDisplay paramEGLDisplay, EGLContext paramEGLContext);
	}

	public static abstract interface Renderer {
		public abstract void onSurfaceCreated(GL10 paramGL10,
											  EGLConfig paramEGLConfig);

		public abstract void onSurfaceChanged(GL10 paramGL10, int paramInt1,
											  int paramInt2);

		public abstract void onDrawFrame(GL10 paramGL10);
	}

	public static abstract interface GLWrapper {
		public abstract GL wrap(GL paramGL);
	}
}