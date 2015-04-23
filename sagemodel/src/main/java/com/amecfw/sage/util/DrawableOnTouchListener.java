package com.amecfw.sage.util;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public abstract class DrawableOnTouchListener implements OnTouchListener {

	protected static final int DRAWABLE_LEFT = 0;
	protected static final int DRAWABLE_RIGHT = 2;
	protected static final int DRAWABLE_TOP = 1;
	protected static final int DRAWABLE_BOTTOM = 3;
	
	private OnClickListener listener;
	
	
	public void setOnClickListener(OnClickListener listener){
		this.listener = listener;
	}
	
	protected void doClick(View v){
		if(listener != null) listener.onClick(v);
	}
	
	public static class DrawableRightOnTouchListener extends DrawableOnTouchListener{
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(!(v instanceof TextView)){
				return false;
			}
			TextView cd = (TextView) v; //cd is compoundDrawable
			if(event.getAction() == MotionEvent.ACTION_UP &&
					(event.getRawX() >= (cd.getRight() - cd.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()))){
				doClick(v);
				return false;
			}
			return true;
		}
	}

}
