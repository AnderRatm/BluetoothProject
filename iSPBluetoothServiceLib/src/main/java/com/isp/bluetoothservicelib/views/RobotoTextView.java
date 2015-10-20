package com.isp.bluetoothservicelib.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class RobotoTextView extends TextView {
	

	public RobotoTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		setRobotoFont(context);
	}

	private void setRobotoFont(Context context){
		Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		setTypeface(tf);
	}
	
	
}
