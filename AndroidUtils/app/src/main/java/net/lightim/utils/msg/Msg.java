package net.lightim.utils.msg;

import net.lightim.utils.TaskUtil;
import net.lightim.utils.Values;

import java.util.ArrayList;

/**
 * entaoyang@163.com
 */
public class Msg {
	public final Values argValues = new Values();//参数
	public final ArrayList<Object> returnValues = new ArrayList<>();//返回值
	public Object sender = null;
	public String msg;
	public long argN = 0;//参数
	public long argN2 = 0;
	public String argS;//参数

	public Msg(String msg) {
		this.msg = msg;
	}

	public static Msg msg(String msg) {
		return new Msg(msg);
	}

	public static Msg msg(Class<?> cls) {
		return new Msg(cls.getName());
	}

	public Msg sender(Object sender) {
		this.sender = sender;
		return this;
	}

	public Msg argN(long argN) {
		this.argN = argN;
		return this;
	}

	public Msg argN2(long argN2) {
		this.argN2 = argN2;
		return this;
	}

	public Msg argS(String arg) {
		this.argS = arg;
		return this;
	}

	public Msg values(Values values) {
		this.argValues.addAll(values);
		return this;
	}

	public void ret(Object obj) {
		returnValues.add(obj);
	}

	public boolean is(String... msgs) {
		for (String msg : msgs) {
			if (this.msg.equals(msg)) {
				return true;
			}
		}
		return false;
	}

	public boolean is(Class<?>... clses) {
		for (Class<?> cls : clses) {
			if (this.msg.equals(cls.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean from(Object source) {
		return this.sender == source;
	}

	public boolean hasReturn(Object retVal) {
		return returnValues.contains(retVal);
	}

	public Msg fire() {
		MsgCenter.fire(this);
		return this;
	}

	public void fireFore() {
		TaskUtil.fore(new Runnable() {
			@Override
			public void run() {
				MsgCenter.fire(Msg.this);
			}
		});
	}

	public void fireBack() {
		TaskUtil.back(new Runnable() {
			@Override
			public void run() {
				MsgCenter.fire(Msg.this);
			}
		});
	}

	@Override
	public int hashCode() {
		return msg.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Msg) {
			return msg.equals(((Msg) o).msg);
		}
		return false;
	}

	@Override
	public String toString() {
//		return JsonUtil.toJson(this);
		return msg;
	}
}
