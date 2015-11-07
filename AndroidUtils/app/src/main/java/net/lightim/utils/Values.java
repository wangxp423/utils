package net.lightim.utils;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Values {
	private ArrayMap<String, Object> map;

	public Values() {
		this(8);
	}

	public Values(int n) {
		map = new ArrayMap<String, Object>(8);
	}

	public Values(Values other) {
		map = new ArrayMap<String, Object>(other.map);
	}

	public static Values from(Values v) {
		return new Values(v);
	}

	public static Values from(Map<String, Object> map) {
		if (map == null) {
			return new Values();
		}
		Values v = new Values(map.size());
		for (Entry<String, Object> e : map.entrySet()) {
			v.put(e.getKey(), e.getValue());
		}
		return v;
	}

	public static Values from(Bundle b) {
		if (b == null) {
			return new Values();
		}
		Values v = new Values(b.size());
		for (String key : b.keySet()) {
			v.put(key, b.get(key));
		}
		return v;
	}

	public static Values from(Intent it) {
		if (it != null) {
			Bundle b = it.getExtras();
			if (b != null) {
				return from(b);
			}
		}
		return new Values();
	}

	public static Values build(String key, Object value, Object... keyValues) {
		Values vs = new Values(1 + keyValues.length);
		vs.put(key, value);
		for (int i = 1; i < keyValues.length; ) {
			String k = (String) keyValues[i - 1];
			Object v = keyValues[i];
			vs.put(k, v);
			i += 2;
		}
		return vs;
	}

	public static Values buildKV(Object... keyValues) {
		Values vs = new Values(1 + keyValues.length);
		for (int i = 1; i < keyValues.length; ) {
			String k = (String) keyValues[i - 1];
			Object v = keyValues[i];
			vs.put(k, v);
			i += 2;
		}
		return vs;
	}

	public ContentValues toContentValues(boolean throwable) {
		return contentValues(throwable);
	}

	public ContentValues contentValues(boolean throwable) {
		ContentValues v = new ContentValues(map.size());
		for (Entry<String, Object> e : map.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();
			if (value == null) {
				v.putNull(key);
			} else if (value instanceof String) {
				v.put(key, (String) value);
			} else if (value instanceof Boolean) {
				v.put(key, (Boolean) value);
			} else if (value instanceof Double) {
				v.put(key, (Double) value);
			} else if (value instanceof Float) {
				v.put(key, (Float) value);
			} else if (value instanceof Integer) {
				v.put(key, (Integer) value);
			} else if (value instanceof Long) {
				v.put(key, (Long) value);
			} else if (value instanceof Byte) {
				v.put(key, (Byte) value);
			} else if (value instanceof Short) {
				v.put(key, (Short) value);
			} else if (value instanceof byte[]) {
				v.put(key, (byte[]) value);
			} else {
				if (throwable) {
					throw new IllegalArgumentException("ContentValues unsupport this type: key=" + key + " value.class=" + value.getClass().getName());
				} else {
					xlog.e("未知的数据类型", key, value);
				}
			}
		}
		return v;
	}

//    /**
//     * @param noThrow 遇到不支持的类型是否抛出异常
//     * @return
//     */
//    public JsonObject toJson(boolean throwable) {
//        JsonObject jo = new JsonObject();
//        for (Entry<String, Object> e : map.entrySet()) {
//            String key = e.getKey();
//            Object value = e.getValue();
//            if (value == null) {
//                jo.setNull(key);
//            } else if (value instanceof JsonElement) {
//                jo.set(key, (JsonElement) value);
//            } else if (value instanceof Values) {
//                jo.set(key, ((Values) value).toJson(throwable));
//            } else if (value instanceof Bundle) {
//                jo.set(key, Values.from((Bundle) value).toJson(throwable));
//            } else if (value instanceof String) {
//                jo.set(key, (String) value);
//            } else if (value instanceof String[]) {
//                jo.set(key, new JsonArray((String[]) value));
//            } else if (value instanceof Boolean) {
//                jo.set(key, (Boolean) value);
//            } else if (value instanceof boolean[]) {
//                jo.set(key, new JsonArray((boolean[]) value));
//            } else if (value instanceof Double) {
//                jo.set(key, (Double) value);
//            } else if (value instanceof double[]) {
//                jo.set(key, new JsonArray((double[]) value));
//            } else if (value instanceof Float) {
//                jo.set(key, (Float) value);
//            } else if (value instanceof float[]) {
//                jo.set(key, new JsonArray((float[]) value));
//            } else if (value instanceof Integer) {
//                jo.set(key, (Integer) value);
//            } else if (value instanceof int[]) {
//                jo.set(key, new JsonArray((int[]) value));
//            } else if (value instanceof Long) {
//                jo.set(key, (Long) value);
//            } else if (value instanceof long[]) {
//                jo.set(key, new JsonArray((long[]) value));
//            } else {
//                if (throwable) {
//                    throw new IllegalArgumentException("JsonObject unsupport this type: key=" + key + " value.class=" + value.getClass().getName());
//                } else {
//                    xlog.e("不支持的数据类型", key, value);
//                }
//            }
//            // else if (value instanceof Byte) {
//            // b.putByte(key, (Byte) value);
//            // } else if (value instanceof byte[]) {
//            // b.putByteArray(key, (byte[]) value);
//            // } else if (value instanceof Short) {
//            // b.putShort(key, (Short) value);
//            // } else if (value instanceof short[]) {
//            // b.putShortArray(key, (short[]) value);
//            // } else if (value instanceof Character) {
//            // b.putChar(key, (Character) value);
//            // } else if (value instanceof char[]) {
//            // b.putCharArray(key, (char[]) value);
//            // } else if (value instanceof CharSequence) {
//            // b.putCharSequence(key, (CharSequence) value);
//            // } else if (value instanceof CharSequence[]) {
//            // b.putCharSequenceArray(key, (CharSequence[]) value);
//            // } else if (value instanceof Parcelable) {
//            // b.putParcelable(key, (Parcelable) value);
//            // } else if (value instanceof Parcelable[]) {
//            // b.putParcelableArray(key, (Parcelable[]) value);
//            // } else if (value instanceof Serializable) {
//            // b.putSerializable(key, (Serializable) value);
//            // } else {
//            // throw new IllegalArgumentException("Bundle unsupport this type: key=" + key + " value.class=" + value.getClass().getName());
//            // }
//        }
//        return jo;
//    }

	public Bundle toBundle(boolean throwable) {
		return bundle(throwable);
	}

	public Bundle bundle(boolean throwable) {
		Bundle b = new Bundle(map.size());
		for (Entry<String, Object> e : map.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();
			if (value == null) {
				b.putString(key, null);
			} else if (value instanceof Values) {
				b.putBundle(key, ((Values) value).bundle(throwable));
			} else if (value instanceof Bundle) {
				b.putBundle(key, (Bundle) value);
			} else if (value instanceof String) {
				b.putString(key, (String) value);
			} else if (value instanceof String[]) {
				b.putStringArray(key, (String[]) value);
			} else if (value instanceof Boolean) {
				b.putBoolean(key, (Boolean) value);
			} else if (value instanceof boolean[]) {
				b.putBooleanArray(key, (boolean[]) value);
			} else if (value instanceof Double) {
				b.putDouble(key, (Double) value);
			} else if (value instanceof double[]) {
				b.putDoubleArray(key, (double[]) value);
			} else if (value instanceof Float) {
				b.putFloat(key, (Float) value);
			} else if (value instanceof float[]) {
				b.putFloatArray(key, (float[]) value);
			} else if (value instanceof Integer) {
				b.putInt(key, (Integer) value);
			} else if (value instanceof int[]) {
				b.putIntArray(key, (int[]) value);
			} else if (value instanceof Long) {
				b.putLong(key, (Long) value);
			} else if (value instanceof long[]) {
				b.putLongArray(key, (long[]) value);
			} else if (value instanceof Byte) {
				b.putByte(key, (Byte) value);
			} else if (value instanceof byte[]) {
				b.putByteArray(key, (byte[]) value);
			} else if (value instanceof Short) {
				b.putShort(key, (Short) value);
			} else if (value instanceof short[]) {
				b.putShortArray(key, (short[]) value);
			} else if (value instanceof Character) {
				b.putChar(key, (Character) value);
			} else if (value instanceof char[]) {
				b.putCharArray(key, (char[]) value);
			} else if (value instanceof CharSequence) {
				b.putCharSequence(key, (CharSequence) value);
			} else if (value instanceof CharSequence[]) {
				b.putCharSequenceArray(key, (CharSequence[]) value);
			} else if (value instanceof Parcelable) {
				b.putParcelable(key, (Parcelable) value);
			} else if (value instanceof Parcelable[]) {
				b.putParcelableArray(key, (Parcelable[]) value);
			} else if (value instanceof Serializable) {
				b.putSerializable(key, (Serializable) value);
			} else {
				if (throwable) {
					throw new IllegalArgumentException("Bundle unsupport this type: key=" + key + " value.class=" + value.getClass().getName());
				} else {
					xlog.e("未知的数据类型", key, value);
				}
			}
		}
		return b;
	}

//    public JsonObject jsonObject() {
//        JsonObject jo = new JsonObject();
//        for (Entry<String, Object> e : map.entrySet()) {
//            String key = e.getKey();
//            Object value = e.getValue();
//            jo.set(key, value);
//        }
//        return jo;
//    }

	public int size() {
		return map.size();
	}

	public String keyAt(int index) {
		return map.keyAt(index);
	}

	public Object valueAt(int index) {
		return map.valueAt(index);
	}

	public Set<String> keySet() {
		return map.keySet();
	}

	public Values addAll(Values values) {
		if (values != null) {
			this.map.putAll((SimpleArrayMap<String, Object>) values.map);
		}
		return this;
	}

	public Values put(String key, Object value) {
		map.put(key, value);
		return this;
	}

	public Values putNull(String key) {
		map.put(key, null);
		return this;
	}

	public Object get(String key) {
		return map.get(key);
	}

	public boolean has(String key) {
		return map.containsKey(key);
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defVal) {
		Object obj = map.get(key);
		if (obj instanceof Boolean) {
			return (Boolean) obj;
		} else if (obj instanceof Number) {
			return ((Number) obj).intValue() != 0;
		} else if (obj instanceof String) {
			return "true".equals(((String) obj).toLowerCase());
		}
		return defVal;
	}

	public int getInt(String key) {
		return getInt(key, 0);
	}

	public int getInt(String key, int defVal) {
		Object obj = map.get(key);
		if (obj instanceof Number) {
			return ((Number) obj).intValue();
		} else if (obj instanceof String) {
			return Integer.parseInt((String) obj);
		}
		return defVal;
	}

	public long getLong(String key) {
		return getLong(key, 0L);
	}

	public long getLong(String key, long defVal) {
		Object obj = map.get(key);
		if (obj instanceof Number) {
			return ((Number) obj).longValue();
		} else if (obj instanceof String) {
			return Long.parseLong((String) obj);
		}
		return defVal;
	}

	public String getString(String key) {
		return getString(key, null);
	}

	public String getString(String key, String defVal) {
		Object obj = map.get(key);
		if (obj instanceof Number) {
			return ((Number) obj).toString();
		} else if (obj instanceof String) {
			return (String) obj;
		} else if (obj != null) {
			return obj.toString();
		}
		return defVal;
	}
}
