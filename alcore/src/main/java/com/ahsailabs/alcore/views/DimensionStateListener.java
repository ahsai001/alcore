package com.ahsailabs.alcore.views;

import android.graphics.Rect;

public interface DimensionStateListener {
	public boolean rectForCurrentDimensionState(Rect currentRectState);
	public boolean indexForCurrentDimensionState(int currentIndexState);
}
