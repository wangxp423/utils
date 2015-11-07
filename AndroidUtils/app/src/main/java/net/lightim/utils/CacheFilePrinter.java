package net.lightim.utils;

import java.io.File;

public class CacheFilePrinter implements xlog.XPrinter {
	xlog.FilePrinter p = null;

	public CacheFilePrinter(File file) {
		p = new xlog.FilePrinter(file, 8000, 4 * 1024 * 1024);
	}

	@Override
	public void flush() {
		if (p != null) {
			p.flush();
		}
	}

	@Override
	public void println(int priority, String tag, String msg) {
		if (p != null) {
			p.println(priority, tag, msg);
		}
	}
}
