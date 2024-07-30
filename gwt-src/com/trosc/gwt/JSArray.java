/**
 * 
 */
package com.trosc.gwt;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * @author Marco
 *
 */
public class JSArray extends JsArray<JavaScriptObject> {

	/**
	 * 
	 */
	protected JSArray() {
		// TODO Auto-generated constructor stub
	}
	
	public native final static <T extends JSArray> T create() /*-{
		return [];
	}-*/;
	
	public native final void setInt(int index, int value) /*-{
		this[index] = value;
	}-*/;
	
	public native final void setDouble(int index, double value) /*-{
		this[index] = value;
	}-*/;
	
	public native final void setString(int index, String value) /*-{
		this[index] = value;
	}-*/;

	public native final void setBoolean(int index, boolean value) /*-{
		this[index] = value;
	}-*/;

	public native final void setChar(int index, char value) /*-{
		this[index] = value;
	}-*/;

	public native final void setShort(int index, short value) /*-{
		this[index] = value;
	}-*/;

	public native final void setByte(int index, byte value) /*-{
		this[index] = value;
	}-*/;
	
	public native final void pushInt(int value) /*-{
		this[this.length] = value;
	}-*/;
	
	public native final void pushDouble(double value) /*-{
		this[this.length] = value;
	}-*/;
	
	public native final void pushString(String value) /*-{
		this[this.length] = value;
	}-*/;
	
	public native final void pushBoolean(boolean value) /*-{
		this[this.length] = value;
	}-*/;
	
	public native final void pushChar(char value) /*-{
		this[this.length] = value;
	}-*/;
	
	public native final void pushShort(short value) /*-{
		this[this.length] = value;
	}-*/;
	
	public native final void pushByte(byte value) /*-{
		this[this.length] = value;
	}-*/;

	public native final int getInt(int index) /*-{
		return this[index];
	}-*/;

	public native final short getShort(int index) /*-{
		return this[index];
	}-*/;

	public native final byte getByte(int index) /*-{
		return this[index];
	}-*/;

	public native final char getChar(int index) /*-{
		return this[index];
	}-*/;

	public native final int getDouble(int index) /*-{
		return this[index];
	}-*/;

	public native final int getString(int index) /*-{
		return this[index];
	}-*/;

	public native final int getBoolean(int index) /*-{
		return this[index];
	}-*/;
	
}
