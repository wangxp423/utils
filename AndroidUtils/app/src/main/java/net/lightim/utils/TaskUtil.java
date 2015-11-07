package net.lightim.utils;

import android.os.Handler;
import android.os.Looper;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class TaskUtil {
	private static Handler main;
	private static ExecutorService es;
	private static UncaughtExceptionHandler exHandler = new UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			ex.printStackTrace();
			xlog.e(ex);
			xlog.flush();
		}
	};

	static {
		main = new Handler(Looper.getMainLooper());
		es = Executors.newCachedThreadPool(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(Thread.NORM_PRIORITY - 1);
				t.setDaemon(true);
				t.setUncaughtExceptionHandler(exHandler);
				return t;
			}
		});
	}

	public static Handler getMainHandler() {
		return main;
	}

	public static ExecutorService getPool() {
		return es;
	}

	public static <T extends RunTask> T foreDelay(long millSec, T t) {
		main.postDelayed(t, millSec);
		return t;
	}

	/**
	 * main handler中运行
	 */
	public static boolean foreDelay(long millSeconds, final Runnable r) {
		return main.postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					r.run();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, millSeconds);
	}

	public static <T extends RunTask> T fore(T t) {
		main.post(t);
		return t;
	}

	/**
	 * main handler中运行
	 */
	public static void fore(final Runnable r) {
		main.post(new Runnable() {

			@Override
			public void run() {
				try {
					r.run();
				} catch (Throwable e) {
					e.printStackTrace();
					xlog.e(e);
				}
			}
		});
	}

	public static <T extends RunTask> T back(T t) {
		es.submit(t);
		return t;
	}

	/**
	 * thread pool中运行
	 */
	public static void back(final Runnable r) {
		es.submit(r);
	}

	public static <T extends RunTask> T backDelay(long minSec, final T t) {
		foreDelay(minSec, new Runnable() {

			@Override
			public void run() {
				back(t);

			}
		});
		return t;
	}

	public static void backDelay(long millSec, final Runnable r) {
		foreDelay(millSec, new Runnable() {

			@Override
			public void run() {
				back(r);
			}
		});
	}

	public static <T extends ForeBackTask> T foreBack(T t) {
		t.setExecutorService(es);
		return fore(t);
	}

	/**
	 * 先main handler中执行task.onFore, 再在线程池中执行task.onBack
	 */
	public static void foreBack(final ForeBack task) {
		fore(new Runnable() {

			@Override
			public void run() {
				try {
					task.onFore();
				} catch (Throwable e) {
					e.printStackTrace();
					xlog.e(e);
				}
				back(new Runnable() {

					@Override
					public void run() {
						try {
							task.onBack();
						} catch (Throwable e) {
							e.printStackTrace();
							xlog.e(e);
						}
					}
				});
			}
		});
	}

	public static <T extends BackForeTask> T backFore(T t) {
		es.submit(t);
		return t;
	}

	/**
	 * 先在线程池中执行task.onBack, 再main handler中执行task.onFore
	 */
	public static void backFore(final BackFore task) {
		back(new Runnable() {

			@Override
			public void run() {
				try {
					task.onBack();
				} catch (Throwable e) {
					e.printStackTrace();
					xlog.e(e);
				}
				fore(new Runnable() {

					@Override
					public void run() {
						try {
							task.onFore();
						} catch (Throwable e) {
							e.printStackTrace();
							xlog.e(e);
						}

					}
				});
			}
		});
	}

	/**
	 * onRepeat总是在主线程回调
	 * onRepeatEnd有可能在当前线程回调, 也有可能在主线程回调
	 * <p/>
	 * repeatFore(callback, 1000, 2000, 3000)
	 * 分别在第1秒, 1+2秒, 1+2+3秒后主线程回调callback, 然后再回调onRepeatEnd
	 * 如果values的length是0, 则只会回调onRepeatEnd
	 *
	 * @param fromIndex >=0
	 * @param callback  !=null
	 * @param values    每个值都>=0
	 */
	private static void repeatFore(final int fromIndex, final IRepeatCallback callback, final long... values) {
		assert (fromIndex >= 0);
		assert (callback != null);
		for (long v : values) {
			assert (v >= 0);
		}

		if (values.length == 0) {
			callback.onRepeatEnd();
			return;
		}
		if (fromIndex >= values.length) {
			callback.onRepeatEnd();
			return;
		}
		foreDelay(values[fromIndex], new Runnable() {

			@Override
			public void run() {
				boolean b = callback.onRepeat(fromIndex, values[fromIndex]);
				if (b) {
					repeatFore(fromIndex + 1, callback, values);
				} else {
					callback.onRepeatEnd();
				}
			}
		});

	}

	/**
	 * onRepeat总是在主线程回调
	 * onRepeatEnd有可能在当前线程回调, 也有可能在主线程回调
	 * <p/>
	 * repeatFore(callback, 1000, 2000, 3000)
	 * 分别在第1秒, 1+2秒, 1+2+3秒后主线程回调callback, 然后再回调onRepeatEnd
	 * 如果values的length是0, 则只会回调onRepeatEnd
	 *
	 * @param callback !=null
	 * @param values   每个值都>=0
	 */
	public static void repeatFore(IRepeatCallback callback, long... values) {
		repeatFore(0, callback, values);
	}

	/**
	 * onRepeat总是在后台线程回调
	 * onRepeatEnd有可能在当前线程回调, 也有可能在后台线程回调
	 * <p/>
	 * repeatFore(callback, 1000, 2000, 3000)
	 * 分别在第1秒, 1+2秒, 1+2+3秒后后台线程回调callback, 然后再回调onRepeatEnd
	 * 如果values的length是0, 则只会回调onRepeatEnd
	 *
	 * @param fromIndex >=0
	 * @param callback  !=null
	 * @param values    每个值都>=0
	 */
	private static void repeatBack(final int fromIndex, final IRepeatCallback callback, final long... values) {
		assert (fromIndex >= 0);
		assert (callback != null);
		for (long v : values) {
			assert (v >= 0);
		}

		if (values.length == 0) {
			callback.onRepeatEnd();
			return;
		}
		if (fromIndex >= values.length) {
			callback.onRepeatEnd();
			return;
		}
		backDelay(values[fromIndex], new Runnable() {

			@Override
			public void run() {
				boolean b = callback.onRepeat(fromIndex, values[fromIndex]);
				if (b) {
					repeatBack(fromIndex + 1, callback, values);
				} else {
					callback.onRepeatEnd();
				}
			}
		});

	}

	/**
	 * onRepeat总是在后台线程回调
	 * onRepeatEnd有可能在当前线程回调, 也有可能在后台线程回调
	 * <p/>
	 * repeatFore(callback, 1000, 2000, 3000)
	 * 分别在第1秒, 1+2秒, 1+2+3秒后后台线程回调callback, 然后再回调onRepeatEnd
	 * 如果values的length是0, 则只会回调onRepeatEnd
	 *
	 * @param callback !=null
	 * @param values   每个值都>=0
	 */
	public static void repeatBack(IRepeatCallback callback, long... values) {
		repeatBack(0, callback, values);
	}

	private static void repeatFore(final int current, final int times, final long delay, final IRepeatCallback callback) {
		assert (current >= 0);
		assert (times >= 0);
		assert (delay >= 0);
		assert (callback != null);

		if (times <= 0) {
			callback.onRepeatEnd();
			return;
		}
		if (current >= times) {
			callback.onRepeatEnd();
			return;
		}

		foreDelay(delay, new Runnable() {

			@Override
			public void run() {
				boolean b = callback.onRepeat(current, delay);
				if (b) {
					repeatFore(current + 1, times, delay, callback);
				} else {
					callback.onRepeatEnd();
				}
			}
		});
	}

	/**
	 * 每隔delay毫秒就执行一次callback, 共执行times次
	 * 如果onRepeat返回false, 则终止后面的回调, 并且回调onRepeatEnd
	 *
	 * @param times    >=0; 0:只回调onRepeatEnd; n:回调n次onRepeat,再回调onRepeatEnd
	 * @param delay    每次回调间隔时间, 第一次回调也会等待delay毫秒
	 * @param callback
	 */
	public static void repeatFore(int times, long delay, IRepeatCallback callback) {
		repeatFore(0, times, delay, callback);
	}

	private static void repeatBack(final int current, final int times, final long delay, final IRepeatCallback callback) {
		assert (current >= 0);
		assert (times >= 0);
		assert (delay >= 0);
		assert (callback != null);

		if (times <= 0) {
			callback.onRepeatEnd();
			return;
		}
		if (current >= times) {
			callback.onRepeatEnd();
			return;
		}

		backDelay(delay, new Runnable() {

			@Override
			public void run() {
				boolean b = callback.onRepeat(current, delay);
				if (b) {
					repeatBack(current + 1, times, delay, callback);
				} else {
					callback.onRepeatEnd();
				}
			}
		});
	}

	/**
	 * 每隔delay毫秒就执行一次callback, 共执行times次
	 * 如果onRepeat返回false, 则终止后面的回调, 并且回调onRepeatEnd
	 *
	 * @param times    >=0; 0:只回调onRepeatEnd; n:回调n次onRepeat,再回调onRepeatEnd
	 * @param delay    每次回调间隔时间, 第一次回调也会等待delay毫秒
	 * @param callback
	 */
	public static void repeatBack(int times, long delay, IRepeatCallback callback) {
		repeatBack(0, times, delay, callback);
	}

}
