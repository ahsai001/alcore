package com.ahsailabs.alcore.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ASLinearLayout extends LinearLayout{
	private int width = 0;
	private int height = 0;
	
	public int getIVtWidth() {
		return width;
	}
	private void setIVWidth(int width) {
		this.width = width;
	}
	public int getIVHeight() {
		return height;
	}
	private void setIVHeight(int height) {
		this.height = height;
	}
	
	public ASLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}
	
	public ASLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public ASLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		setIVWidth(w);
		setIVHeight(h);
	}

}
