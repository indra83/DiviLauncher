package co.in.divi.launcher;

import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;

public class Util {

	public static void fixBackgroundRepeat(View view) {
		Drawable bg = view.getBackground();
		if (bg != null) {
			if (bg instanceof LayerDrawable) {
				// Log.d("temp", "Hooray! setting bitmap tile mode");
				BitmapDrawable bd = (BitmapDrawable) ((LayerDrawable) bg).getDrawable(0);
				bd.mutate(); // make sure that we aren't sharing state anymore
				bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
			} else if (bg instanceof StateListDrawable) {
				StateListDrawable stateDrwbl = (StateListDrawable) bg;
				stateDrwbl.mutate(); // make sure that we aren't sharing state
										// anymore

				ConstantState constantState = stateDrwbl.getConstantState();
				if (constantState instanceof DrawableContainerState) {
					DrawableContainerState drwblContainerState = (DrawableContainerState) constantState;
					final Drawable[] drawables = drwblContainerState.getChildren();
					for (Drawable drwbl : drawables) {
						if (drwbl instanceof LayerDrawable) {
							BitmapDrawable bd = (BitmapDrawable) ((LayerDrawable) drwbl).getDrawable(0);
							// bd.mutate(); // make sure that we aren't sharing
							// state anymore
							bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
						}
					}
				}
			}
		}
	}

}
