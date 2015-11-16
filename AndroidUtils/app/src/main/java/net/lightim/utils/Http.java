package net.lightim.utils;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

/**
 * Created by yet on 2015/11/15.
 * entaoyang@163.com
 */
public class Http {
	public static class Result {
		public byte[] response;//如果Http.request参数给定了文件参数, 则,response是null
		public int responseCode = 0;//200
		public String responseMsg;//OK
		public String contentType;//text/html;charset=utf-8
		public int contentLength;//如果是gzip格式, 这个值!=response.length
		public Map<String, List<String>> headerMap;

		public boolean OK() {
			return responseCode >= 200 && responseCode < 300;
		}

		private static String contentCharset(String contentType, String defCharset) {
			if (contentType != null) {
				String[] arr = contentType.split(";");
				for (String s : arr) {
					s = s.trim();
					int m = s.indexOf('=');
					if (s.startsWith("charset") && m >= 7) {
						String charset = s.substring(m + 1).trim();
						if (charset != null && charset.length() >= 2) {
							return charset;
						}
						break;
					}
				}
			}
			return defCharset;
		}

		public String str(String defCharset) {
			if (OK()) {
				String charset = contentCharset(contentType, defCharset);
				if (charset != null) {
					try {
						return new String(response, defCharset);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				return new String(response);
			}
			return null;
		}

		public String strUtf8() {
			return str(UTF8);
		}

//		public JsonArray gsonArray() {
//			if (OK()) {
//				String s = strUtf8();
//				if (s != null && s.length() > 0) {
//					JsonParser parser = new JsonParser();
//					return (JsonArray) parser.parse(s);
//				}
//			}
//			return null;
//		}
//
//		public JsonObject gsonObject() {
//			if (OK()) {
//				String s = strUtf8();
//				if (s != null && s.length() > 0) {
//					JsonParser parser = new JsonParser();
//					return (JsonObject) parser.parse(s);
//				}
//			}
//			return null;
//		}

		public JSONObject jsonObject() {
			if (OK()) {
				String s = strUtf8();
				if (s != null && s.length() > 0) {
					try {
						return new JSONObject(s);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		public JSONArray jsonArray() {
			if (OK()) {
				String s = strUtf8();
				if (s != null && s.length() > 0) {
					try {
						return new JSONArray(s);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		public byte[] bytes() {
			if (OK()) {
				return response;
			}
			return null;
		}

		public boolean saveTo(File file) {
			if (OK()) {
				File dir = file.getParentFile();
				if (!dir.exists() && !dir.mkdirs()) {
					return false;
				}
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(file);
					fos.write(response);
					fos.flush();
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					close(fos);
				}
			}
			return false;
		}

	}

	public interface Progress {
		void onStart(int total);

		void onProgress(int current, int total, int percent);

		void onFinish();
	}

	public static final String UTF8 = "UTF-8";
	private static Charset charsetUtf8 = Charset.forName(UTF8);
	private static final String BOUNDARY = UUID.randomUUID().toString();
	private static final String BOUNDARY_START = "--" + BOUNDARY + "\r\n";
	private static final String BOUNDARY_END = "--" + BOUNDARY + "--\r\n";
	private static final int GET = 0;
	private static final int POST = 1;
	private static final int MULTIPART = 2;//其实是post的一种
	private static final int PROGRESS_DELAY = 100;

	private Map<String, String> headerMap = new HashMap<>();
	private Map<String, String> argMap = new HashMap<>();
	private Map<String, File> fileMap = new HashMap<>();
	private Map<String, Progress> progressMap = new HashMap<>();
	private int method = GET;
	private String url;
	private int timeoutConnect = 10000;
	private int timeoutRead = 10000;
	private byte[] rawData;

	private Http() {
		accept("*/*");
		acceptLanguage("zh-CN,en-US;q=0.8,en;q=0.6");
		headerMap.put("Accept-Charset", "UTF-8,*");
		headerMap.put("Connection:", "close");
		headerMap.put("Charset", UTF8);
	}

	public static Http get(String url) {
		Http http = new Http();
		http.url = url;
		http.method = GET;
		http.headerMap.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		return http;
	}

	public static Http post(String url) {
		Http http = new Http();
		http.url = url;
		http.method = POST;
		http.headerMap.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		return http;
	}

	public static Http multipart(String url) {
		Http http = new Http();
		http.url = url;
		http.method = MULTIPART;
		http.headerMap.put("Content-Type", "multipart/form-data; boundary=" + BOUNDARY + ";charset=utf-8");
		return http;
	}

	public static Http postData(String url, String contentType, byte[] data) {
		Http http = new Http();
		http.url = url;
		http.method = POST;
		http.headerMap.put("Content-Type", contentType);
		http.rawData = data;
		return http;
	}

	public static Http postDataJson(String url, String json) {
		return postData(url, "text/json;charset=utf-8", json.getBytes(charsetUtf8));
	}

	public static Http postDataXml(String url, String xml) {
		return postData(url, "text/xml;charset=utf-8", xml.getBytes(charsetUtf8));
	}

	public Http header(String key, String value) {
		headerMap.put(key, value);
		return this;
	}

	public Http timeoutConnect(int millSeconds) {
		this.timeoutConnect = millSeconds;
		return this;
	}

	public Http timeoutRead(int millSeconds) {
		this.timeoutRead = millSeconds;
		return this;
	}

	/**
	 * @param accept "* / *", " plain/text"
	 * @return
	 */
	public Http accept(String accept) {
		headerMap.put("Accept", accept);
		return this;
	}

	public Http acceptLanguage(String acceptLanguage) {
		headerMap.put("Accept-Language", acceptLanguage);
		return this;
	}

	public Http auth(String user, String pwd) {
		String usernamePassword = user + ":" + pwd;
		String encodedUsernamePassword = Base64.encodeToString(usernamePassword.getBytes(charsetUtf8), Base64.NO_WRAP);
//		String encodedUsernamePassword = Base64.getEncoder().encodeToString(usernamePassword.getBytes(charsetUtf8));
		headerMap.put("Authorization", "Basic " + encodedUsernamePassword);

		return this;
	}

	public Http arg(String key, String value) {
		argMap.put(key, value);
		return this;
	}

	public Http arg(String key, long value) {
		argMap.put(key, "" + value);
		return this;
	}

	public Http file(String key, File file) {
		fileMap.put(key, file);
		return this;
	}

	public Http file(String key, File file, Progress progress) {
		progressMap.put(key, progress);
		return file(key, file);
	}

	/**
	 * [from, to]
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public Http range(int from, int to) {
		headerMap.put("Range", "bytes=" + from + "-" + to);
		return this;
	}

	public Http range(int from) {
		headerMap.put("Range", "bytes=" + from + "-");
		return this;
	}

	private void preConnect(HttpURLConnection connection) throws ProtocolException, UnsupportedEncodingException {
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setConnectTimeout(timeoutConnect);
		connection.setReadTimeout(timeoutRead);
		if (method == GET) {
			connection.setRequestMethod("GET");
		} else {
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
		}

		for (Map.Entry<String, String> e : headerMap.entrySet()) {
			connection.setRequestProperty(e.getKey(), e.getValue());
		}
		if (fileMap.size() > 0) {
			connection.setChunkedStreamingMode(0);
		}
	}

	private void write(OutputStream os, String... arr) throws IOException {
		for (String s : arr) {
			os.write(s.getBytes(UTF8));
		}
	}

	private void sendMultipart(OutputStream os) throws IOException {
		if (argMap.size() > 0) {
			for (Map.Entry<String, String> e : argMap.entrySet()) {
				write(os, BOUNDARY_START);
				write(os, "Content-Disposition: form-data; name=\"", e.getKey(), "\"\r\n");
				write(os, "Content-Type:text/plain;charset=utf-8\r\n");
				write(os, "\r\n");
				write(os, e.getValue(), "\r\n");
			}
		}
		if (fileMap.size() > 0) {
			for (Map.Entry<String, File> e : fileMap.entrySet()) {
				File file = e.getValue();
				write(os, BOUNDARY_START);
				write(os, "Content-Disposition:form-data;name=\"", e.getKey(), "\";filename=\"", file.getName(), "\"\r\n");
				write(os, "Content-Type:application/octet-stream\r\n");
				write(os, "Content-Transfer-Encoding: binary\r\n");
				write(os, "\r\n");
				Progress progress = progressMap.get(e.getKey());
				FileInputStream fis = new FileInputStream(file);
				try {
					int total = fis.available();
					if (progress != null) {
						progress.onStart(total);
					}
					byte[] buffer = new byte[4096];
					int count = -1;
					int sent = 0;
					long pre = System.currentTimeMillis();
					while ((count = fis.read(buffer)) != -1) {
						os.write(buffer, 0, count);
						sent += count;
						if (progress != null) {
							long cur = System.currentTimeMillis();
							if (cur - pre > PROGRESS_DELAY) {
								pre = cur;
								progress.onProgress(sent, total, total > 0 ? sent * 100 / total : 0);
							}
						}
					}

					if (progress != null) {
						progress.onProgress(sent, total, total > 0 ? sent * 100 / total : 0);
					}
				} finally {
					close(fis);
					if (progress != null) {
						progress.onFinish();
					}
				}
				write(os, "\r\n");
			}
		}
		os.write(BOUNDARY_END.getBytes());
	}

	private String buildArgs() {
		StringBuilder sb = new StringBuilder(argMap.size() * 32 + 16);
		for (Map.Entry<String, String> e : argMap.entrySet()) {
			try {
				String name = URLEncoder.encode(e.getKey(), UTF8);
				String val = URLEncoder.encode(e.getValue(), UTF8);
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append(name);
				sb.append("=");
				sb.append(val);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return sb.toString();
	}

	public String buildGetUrl() throws MalformedURLException {
		String sArgs = buildArgs();
		String u = url;
		if (sArgs.length() > 0) {
			int n = u.indexOf('?');
			if (n < 0) {
				u = u + "?";
			}
			if ('?' != u.charAt(u.length() - 1)) {
				u = u + "&";
			}
			u = u + sArgs;
		}
		return u;
	}

	private Result onResponse(HttpURLConnection connection, Progress progress, File saveToFile) throws IOException {
		Result result = new Result();
		result.responseCode = connection.getResponseCode();
		result.responseMsg = connection.getResponseMessage();
		result.contentType = connection.getContentType();
		result.headerMap = connection.getHeaderFields();
		int total = connection.getContentLength();
		result.contentLength = total;

		if (result.OK()) {
			if (progress != null) {
				progress.onStart(total);
			}
			InputStream is = null;
			OutputStream os = null;
			try {
				is = connection.getInputStream();
				String mayGzip = connection.getContentEncoding();
				if (mayGzip != null && mayGzip.contains("gzip")) {
					is = new GZIPInputStream(is);
				}

				if (saveToFile != null) {
					os = new FileOutputStream(saveToFile);
				} else {
					os = new ByteArrayOutputStream(total > 0 ? total : 64);
				}

				byte[] buf = new byte[4096];
				int n = -1;
				long pre = System.currentTimeMillis();
				int recv = 0;
				while ((n = is.read(buf)) != -1) {
					os.write(buf, 0, n);
					recv += n;
					if (progress != null) {
						long curr = System.currentTimeMillis();
						if (curr - pre > PROGRESS_DELAY) {
							pre = curr;
							progress.onProgress(recv, total, total > 0 ? recv * 100 / total : 0);
						}
					}
				}
				os.flush();
				if (os instanceof ByteArrayOutputStream) {
					result.response = ((ByteArrayOutputStream) os).toByteArray();
				}
				if (progress != null) {
					progress.onProgress(recv, total, total > 0 ? recv * 100 / total : 0);
				}
			} finally {
				close(is);
				close(os);
				if (progress != null) {
					progress.onFinish();
				}
			}
		}
		return result;
	}

	private void onSend(HttpURLConnection connection) throws IOException {
		OutputStream os = connection.getOutputStream();
		try {
			if (method == GET) {

			} else if (method == POST) {
				if (rawData != null) {
					os.write(rawData);
				} else {
					String s = buildArgs();
					if (s.length() > 0) {
						write(os, s);
					}
				}
			} else if (method == MULTIPART) {
				sendMultipart(os);
			}
			os.flush();
		} finally {
			close(os);
		}
	}

	public Result request() {
		return request(null, null);
	}

	public Result request(Progress progress) {
		return request(null, progress);
	}

	public Result request(File saveToFile) {
		return request(saveToFile, null);
	}

	public Result request(File saveToFile, Progress progress) {
		HttpURLConnection connection = null;
		try {
			if (method == GET || rawData != null) {
				connection = (HttpURLConnection) new URL(buildGetUrl()).openConnection();
			} else {
				connection = (HttpURLConnection) new URL(url).openConnection();
			}
			preConnect(connection);
			connection.connect();
			onSend(connection);
			return onResponse(connection, progress, saveToFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return new Result();
	}

	private static void close(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
